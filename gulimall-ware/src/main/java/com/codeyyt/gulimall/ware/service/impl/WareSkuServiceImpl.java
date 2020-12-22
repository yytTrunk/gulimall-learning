package com.codeyyt.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.codeyyt.gulimall.common.exception.NoStockException;
import com.codeyyt.gulimall.common.to.SkuHasStockVo;
import com.codeyyt.gulimall.common.to.mq.OrderTo;
import com.codeyyt.gulimall.common.to.mq.StockDetailTo;
import com.codeyyt.gulimall.common.to.mq.StockLockedTo;
import com.codeyyt.gulimall.common.utils.R;
import com.codeyyt.gulimall.ware.entity.*;
import com.codeyyt.gulimall.ware.feign.OrderFeignService;
import com.codeyyt.gulimall.ware.feign.ProductFeignService;
import com.codeyyt.gulimall.ware.service.WareOrderTaskDetailService;
import com.codeyyt.gulimall.ware.service.WareOrderTaskService;
import com.codeyyt.gulimall.ware.vo.OrderItemVo;
import com.codeyyt.gulimall.ware.vo.OrderVo;
import com.codeyyt.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codeyyt.gulimall.common.utils.PageUtils;
import com.codeyyt.gulimall.common.utils.Query;

import com.codeyyt.gulimall.ware.dao.WareSkuDao;
import com.codeyyt.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(entities == null || entities.size() == 0){
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");

                if(info.getCode() == 0){
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){

            }


            wareSkuDao.insert(skuEntity);
        }else{
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(item -> {
            SkuHasStockVo output = new SkuHasStockVo();
            // 查询当前sku的总库存量
            Long count = this.baseMapper.getSkuStock(item);
            output.setSkuId(item);
            output.setHasStock(count != null && count > 0L);
            return output;
        }).collect(Collectors.toList());
        return collect;
    }

    // 库存解锁订单
    // 1. 下订单成功，订单过期，没有支付、用户手动解锁
    // 2. 下订单成功，锁库存成功，但是执行后面流程时失败，需要回滚
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public void orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存工作单的详情
         * 追溯
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            List<Long> wareIds = this.baseMapper.listWareIdsHasSkuStock(skuId);
            stock.setWareIds(wareIds);

            return stock;
        }).collect(Collectors.toList());

        for (SkuWareHasStock stock : collect) {
            Boolean skuStocked = false;
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareIds();
            Integer num = stock.getNum();
            if(wareIds == null || wareIds.size() == 0){
                throw new NoStockException();
            }
            for (Long wareId : wareIds) {
                Long col = this.baseMapper.lockSkuStock(skuId,wareId,num);
                if(col == 1){
                    skuStocked = true;
                    // TODO 通知MQ库存锁定成功
                    // 保存锁成功工作单到数据库中
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                    wareOrderTaskDetailEntity.setSkuId(skuId);
                    wareOrderTaskDetailEntity.setSkuNum(num);
                    wareOrderTaskDetailEntity.setTaskId(wareOrderTaskEntity.getId());
                    wareOrderTaskDetailEntity.setWareId(wareId);
                    wareOrderTaskDetailEntity.setLockStatus(1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity,detailTo);
                    lockedTo.setDetail(detailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked", lockedTo);

                    break;
                }
            }
            if(skuStocked == false){
                throw new NoStockException();
            }
        }
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if(byId != null){
            // 订单存在，进行解锁
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            // TODO 查订单状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode()==0) {
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                // 判断订单是否已取消，如果取消，就进行解锁
                if(data == null || data.getStatus() == 4){
                    if(detail.getLockStatus() == 1){
                        unlockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detailId);
                    }
                }
            }else{
                throw new RuntimeException("远程服务失败");
            }

        }
    }

    @Transactional
    @Override
    public void unlockStock(OrderTo to) {
        String orderSn = to.getOrderSn();
        // 查库存解锁状态
        WareOrderTaskEntity taskEntity =  wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long taskId = taskEntity.getId();
        // 没有解锁的库存进行解锁
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskId).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            unlockStock(entity.getSkuId(),entity.getWareId(), entity.getSkuNum(),entity.getId());
        }
    }

    @Override
    public void unlockStock(Long skuId, Long wareId, Integer num, Long taskDetailId){
        this.baseMapper.unlockStock(skuId, wareId,num);
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(entity);
    }
}