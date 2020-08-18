package com.codeyyt.gulimall.product.service.impl;

import com.codeyyt.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
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