package com.fangjian.service.order.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.fangjian.service.order.client.ProduceClient;
import com.fangjian.service.order.service.ProduceOrderService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fangjian
 * @Description: 商品订单实现类
 * @date 2019/7/15 下午2:05
 */
@Slf4j
@Service
public class ProduceOrderServiceImpl implements ProduceOrderService {

    @Autowired
    private ProduceClient produceClient;

    @Override
    public int save(int userId, int produceId, int total) {
    	log.info("ProduceClient:远程调用查询商品库存信息");
        //下单之前肯定要 检查该商品是否存在 库存是否够
        String response = produceClient.findById(produceId);
        //Json字符串转换成JsonNode对象
        JSONObject jsonObject = JSONObject.parseObject(response);
        Integer store = jsonObject.getInteger("store");
        String produceName = jsonObject.getString("produceName");
        if (store == null) {
            log.info("找不到商品消息，商品ID = {}", produceId);
            return 1;
        }
        log.info("商品存在,商品ID = {},商品当前库存 = {}", produceId, store, produceName);
        // 如果实际库存小于库存
        if (store - total < 0) {
            log.info("库存不足，扣减失败。商品ID = {},商品当前库存 = {},所需库存 = {}，分布式事务key = {}", produceId, store, total);
            return 1;
        }
        log.info("ProduceOrderServiceImpl:发现可以正常保存本地订单，执行本地save方法，然后正常码0");
        log.info("===订单模块=== 本地事务执行成功,订单生成成功");
        return 0;
    }
}
