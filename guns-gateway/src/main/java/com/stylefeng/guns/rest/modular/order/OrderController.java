package com.stylefeng.guns.rest.modular.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.plugins.Page;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.stylefeng.guns.core.util.TokenBucket;
import com.stylefeng.guns.order.OrderServiceAPI;
import com.stylefeng.guns.order.vo.OrderVO;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @AUTHOR :yuankejia
 * @DESCRIPTION:
 * @DATE:CRETED: IN 14:15 2019/11/6
 * @MODIFY:
 */

/**
 * 订单表的拆分分为横向拆分和纵向拆分
 *
 * dubbo的分组的概念：
 *      dubbo中同一个serviceApi下面可以有多个的provider，如果我们不指定分组，那么消费者调用的时候也不用指定分组，dubbo会随机
 *      调用多个provider中的一个。但是如果我们添加了分组，此时我们消费者调用的时候就要加上分组信息了。
 *      这只是分组的一个作用。分组的作用其实就是将各个服务进行分开，不一定要分开实现相同接口的服务。
 *
 * dubbo的分组有什么用呢？
 *      dubbo的分组可以用来帮我们解决红绿上线的问题，就是当我们有新的服务需要线上测试的时候，可以设置两个分组，两个分组中分别保存着
 *      新的和旧的版本。新的版本我们可以进行线上的测试，而旧的版本就是当新的版本出现问题的时候，可以及时地将旧的版本替换上去。这样就避免了服务
 *      被暂停。
 *
 *      同时也可以用dubbo分组来进行压力测试。
 *
 * dubbo的分组聚合
 *      按组合并返回结果，比如菜单服务，接口也是一样，但是有多种实现，用group区分，现在消费方需要从每个group中调用一次返回结果
 *      合并结果返回，这样就可以实现聚合菜单项。具体的到dubbo的官方文档上可以看到。
 *      注意这里聚合的是
 *
 * dubbo的版本控制：
 *
 *
 * dubbo如何实现接口限流？
 *      之前说过dubbo可以通过控制并发与连接的数量进行限流。但是一般都不会采用这种方式进行限流。
 *      可以使用漏桶法和令牌桶算法进行限流。
 *      漏桶法：将接收到的请求不直接处理，而是将这些请求放入到一个漏桶当中。然后业务系统以固定的频率去处理这些请求，这个漏桶的实现
 *      其实就是一个对列。这种方式可以防止我们的系统被突然地大量请求冲垮。
 *
 *      令牌桶算法：维护一个令牌桶，里面保存了有限个令牌。然后在维护一个守护线程，这个线程会对每一个请求进行处理。来了一个请求，如果还有
 *      令牌，那么守护线程分一个给他，让他进入对列等待被执行，如果没有令牌了，就要等待令牌桶里重新流入令牌。（注意，令牌是按照时间来
 *      恢复的，一般来说有一个恢复的速率。）
 *
 *      两个算法的区别：漏桶法只能以固定的频率去处理请求，而令牌桶如果桶里有1000个令牌，那么就可以同时处理1000个请求，所以说令牌桶算法
 *      对于请求处理峰值来说更大。他的容忍度更大。
 *
 * 使用hystrix
 *
 */
@Slf4j
@RestController
@RequestMapping(value = "/order/")
public class OrderController {
    //得到一个令牌桶
    private static TokenBucket tokenBucket = new TokenBucket();

    @Reference(
            interfaceClass = OrderServiceAPI.class,
            check = false,
            group = "order2018")
    private OrderServiceAPI orderServiceAPI;


    @Reference(
            interfaceClass = OrderServiceAPI.class,
            check = false,
            group = "order2017")
    private OrderServiceAPI orderServiceAPI2017;

    /**
     * 这个方法是Hystrix在调用业务方法时出现了失败，那么就会调用这个回调方法。
     * @param fieldId
     * @param soldSeats
     * @param seatsName
     * @return
     */
    public ResponseVO error(Integer fieldId,String soldSeats,String seatsName){
        return ResponseVO.serviceFail("抱歉，下单的人太多了，请稍后重试");
    }

    // 购票
    /*
        信号量隔离
        线程池隔离
        线程切换
     */
    @HystrixCommand(fallbackMethod = "error", commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"),
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "4000"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50")},
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "1"),
                    @HystrixProperty(name = "maxQueueSize", value = "10"),
                    @HystrixProperty(name = "keepAliveTimeMinutes", value = "1000"),
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "8"),
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "12"),
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "1500")
            })
    @RequestMapping(value = "buyTickets",method = RequestMethod.POST)
    public ResponseVO buyTickets(Integer fieldId, String soldSeats, String seatsName){

        /**
         * 在实际的开发中isTrueSeats，isNotSoldSeats，saveOrderInfo这三个方法都是以特别耗时的操作。所以一般都是以
         * 以异步的方式来使三个方法一起开始执行。但是这里就要考虑这个方法的事务控制了。
         */
        if(tokenBucket.getToken()){
            // 验证售出的票是否为真
            boolean isTrue = orderServiceAPI.isTrueSeats(fieldId+"",soldSeats);

            // 已经销售的座位里，有没有这些座位
            boolean isNotSold = orderServiceAPI.isNotSoldSeats(fieldId+"",soldSeats);

            // 验证，上述两个内容有一个不为真，则不创建订单信息
            if(isTrue && isNotSold){
                // 创建订单信息,注意获取登陆人
                String userId = CurrentUser.getCurrentUser();
                if(userId == null || userId.trim().length() == 0){
                    return ResponseVO.serviceFail("用户未登陆");
                }
                OrderVO orderVO = orderServiceAPI.saveOrderInfo(fieldId,soldSeats,seatsName,Integer.parseInt(userId));
                if(orderVO == null){
                    log.error("购票未成功");
                    return ResponseVO.serviceFail("购票业务异常");
                }else{
                    return ResponseVO.success(orderVO);
                }
            }else{
                return ResponseVO.serviceFail("订单中的座位编号有问题");
            }
        }else{
            return ResponseVO.serviceFail("购票人数过多，请稍后再试");
        }
    }


    @RequestMapping(value = "getOrderInfo",method = RequestMethod.POST)
    public ResponseVO getOrderInfo(
            @RequestParam(name = "nowPage",required = false,defaultValue = "1")Integer nowPage,
            @RequestParam(name = "pageSize",required = false,defaultValue = "5")Integer pageSize
    ){

        // 获取当前登陆人的信息
        String userId = CurrentUser.getCurrentUser();

        // 使用当前登陆人获取已经购买的订单
        Page<OrderVO> page = new Page<>(nowPage,pageSize);
        if(userId != null && userId.trim().length()>0){
            //这个服务是2018的服务，之所以这样写是因为默认的是采用的是2018的服务。
            Page<OrderVO> result = orderServiceAPI.getOrderByUserId(Integer.parseInt(userId), page);
            //这个服务是2017的服务
            Page<OrderVO> result2017 = orderServiceAPI2017.getOrderByUserId(Integer.parseInt(userId), page);

            log.error(result2017.getRecords()+" , "+result.getRecords());

            // 合并结果
            int totalPages = (int)(result.getPages() + result2017.getPages());
            // 2017和2018的订单总数合并
            List<OrderVO> orderVOList = new ArrayList<>();
            orderVOList.addAll(result.getRecords());
            orderVOList.addAll(result2017.getRecords());

            return ResponseVO.success(nowPage,totalPages,"",orderVOList);

        }else{
            return ResponseVO.serviceFail("用户未登陆");
        }
    }

}
