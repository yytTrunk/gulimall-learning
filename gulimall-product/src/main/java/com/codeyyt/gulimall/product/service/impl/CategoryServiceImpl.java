package com.codeyyt.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.codeyyt.gulimall.product.service.CategoryBrandRelationService;
import com.codeyyt.gulimall.product.vo.Catalog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
import org.springframework.util.StringUtils;


@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redisson;

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


    /**
     * 加锁解决缓存穿透问题
     */
    @Cacheable(value = "category",key="'catalogJson'",sync = true)
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        /** TODO 缓存常见问题
         * 1 空结果缓存：缓存穿透
         * 2 设置过期时间（加随机值）：解决缓存穿透
         * 3 加锁：解决缓存击穿
         */

        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");

        if (StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();
//                String jsonRet = JSON.toJSONString(catalogJsonFromDb);
//                stringRedisTemplate.opsForValue().set("catalogJSON", jsonRet, 1, TimeUnit.MINUTES);

            return  catalogJsonFromDb;
        }

        log.info("从redis获取");
        Map<String, List<Catalog2Vo>> parseObject = JSON.parseObject(catalogJSON,
                new TypeReference<Map<String, List<Catalog2Vo>>>() { });
        return parseObject;
    }
/*
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            log.info("读取数据库获取");
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDb();
            String jsonRet = JSON.toJSONString(catalogJsonFromDb);
            stringRedisTemplate.opsForValue().set("catalogJSON", jsonRet, 1, TimeUnit.MINUTES);

            return  catalogJsonFromDb;
        }

        log.info("从redis获取");
        Map<String, List<Catalog2Vo>> parseObject = JSON.parseObject(catalogJSON,
                new TypeReference<Map<String, List<Catalog2Vo>>>() { });
        return parseObject;
    }*/

    /**
     * 采用分布式解决缓存击穿--- redisson
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        // 占分布式锁。去redis占坑
        RLock lock = redisson.getLock("catalogJson-lock");

        lock.lock();
        Map<String, List<Catalog2Vo>> catalogJsonFromDb = null;
        try {
            catalogJsonFromDb = getDataFromDB();
        } finally {
            lock.unlock();
        }

        return catalogJsonFromDb;
    }

    /**
     * 采用分布式解决缓存击穿--- 朴素redis方式
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        // 使用分布式锁
        String uuid = UUID.randomUUID().toString();
        Boolean getLockSuccess = stringRedisTemplate.opsForValue().setIfAbsent("catalog-lock", uuid, 30, TimeUnit.SECONDS);
        if (!getLockSuccess) {
            // 加锁失败
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();
        } else {
            // 加锁成功
            // 设置过期时间,非原子性
            //stringRedisTemplate.expire("catalog-lock", 30, TimeUnit.SECONDS);
            try {
                Map<String, List<Catalog2Vo>> result = getDataFromDB();
                // 执行成功，释放锁，释放锁需要保证原子性，使用lua脚本
                // stringRedisTemplate.delete("catalog-lock");
                return result;
            } finally {
                //使用lua简本脚本解锁，保证原子性
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then  return redis.call('del',KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("catalog-lock"), uuid);
            }
        }
    }

    private Map<String, List<Catalog2Vo>> getDataFromDB() {
        // 加锁进来后，先判断是否存在
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            // 如果存在，直接返回
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON,
                    new TypeReference<Map<String, List<Catalog2Vo>>>() {
                    });
            return result;
        }

        log.info("从数据库中读取");
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

        // 查询完放数据库
        String jsonRet = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set("catalogJSON", jsonRet, 1, TimeUnit.MINUTES);

        return map;
    }

    /**
     * 采用本地加锁解决缓存击穿问题，不适用于分布式环境
     */
    private Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithLocalLock() {
        // 加锁，判断两次缓存中是否存在，避免重复查数据库
        // 但是采用本地锁，不适用于分布式环境下，需要改进为分布式锁
        synchronized (this) {
            // 加锁进来后，先判断是否存在
            return getDataFromDB();
        }
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