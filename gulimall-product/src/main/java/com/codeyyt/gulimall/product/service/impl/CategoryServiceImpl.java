package com.codeyyt.gulimall.product.service.impl;

import com.codeyyt.gulimall.product.service.CategoryBrandRelationService;
import com.codeyyt.gulimall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codeyyt.gulimall.common.utils.PageUtils;
import com.codeyyt.gulimall.common.utils.Query;

import com.codeyyt.gulimall.product.dao.CategoryDao;
import com.codeyyt.gulimall.product.entity.CategoryEntity;
import com.codeyyt.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 查出所有数据
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        // 写法1 复杂麻烦写法
        // 组装成树形结构
        // 1级标题的父id都为0,查询出来为集合
        List<CategoryEntity> firstId = new ArrayList<>();
        for (CategoryEntity entity : categoryEntities) {
            if (entity.getParentCid() == 0) {
                //firstId.add(entity);
                // 查找对应二级分类
                List<CategoryEntity> secondId = new ArrayList<>();
                for (CategoryEntity entity2 : categoryEntities) {
                    if (entity2.getParentCid() == entity.getCatId() && entity2.getCatLevel() == 2) {
                        List<CategoryEntity> thirdId = new ArrayList<>();
                        for (CategoryEntity entity3 : categoryEntities) {
                            if (entity3.getParentCid() == entity2.getCatId() && entity3.getCatLevel() == 3) {
                                thirdId.add(entity3);
                            }
                        }
                        entity2.setChildren(thirdId);
                        secondId.add(entity2);
                    }
                }
                entity.setChildren(secondId);
                firstId.add(entity);
            }
        }

        return firstId;

        // 写法2 优化简单写法
//        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
//                categoryEntity.getParentCid() == 0
//        ).map((menu)->{
//            menu.setChildren(getChildrens(menu,entities));
//            return menu;
//        }).sorted((menu1,menu2)->{
//            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
//        }).collect(Collectors.toList());

    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Category() {
        return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Cacheable(value = "category",key="'catalogJson'",sync = true)
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        // 从数据库查出所有分类
        List<CategoryEntity> categoryAll = this.baseMapper.selectList(null);


        List<CategoryEntity> level1Category = getParent_cid(categoryAll, 0L);

        Map<String, List<Catalog2Vo>> map = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> level2Category = getParent_cid(categoryAll, v.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (level2Category != null && level1Category.size() > 0) {
                catalog2Vos = level2Category.stream().map(item -> {

                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());

                    List<CategoryEntity> level3Category = getParent_cid(categoryAll, item.getCatId());
                    if (level3Category != null && level3Category.size() > 0) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3Category.stream().map(level3 -> {
                            return new Catalog2Vo.Catalog3Vo(item.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        return map;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> categoryAll, Long parentCid) {
        return categoryAll.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        //return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;

    }

    //递归查找所有菜单的子菜单
/*
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity,all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            //2、菜单的排序
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }
*/


}