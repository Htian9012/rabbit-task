package com.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    /*
    //定义一条叫queueA的消息队列
    @Bean
    public Queue queueA() {
        return new Queue("queueA",true);
    }
    //////////////////////// 点对点 - end //////////////////////////



    //定义一条叫queueB1的消息队列
    @Bean(name="queueB1")
    public Queue queueB1() {
        return new Queue("queueB1");
    }
    //定义一条叫queueB2的消息队列
    @Bean(name="queueB2")
    public Queue queueB2() {
        return new Queue("queueB2");
    }

    //定义一个fanout类型的交换机
    @Bean(name="fanoutExchange-queueB")
    FanoutExchange fanoutExchange() {
        return new FanoutExchange("fanoutExchange-queueB");
    }
    //把队列queueB1绑定到交换机fanoutExchange-queueB
    @Bean
    Binding bindingExchangeB1(@Qualifier("queueB1") Queue queue, @Qualifier("fanoutExchange-queueB") FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }
    //把队列queueB2绑定到交换机fanoutExchange-queueB
    @Bean
    Binding bindingExchangeB2(@Qualifier("queueB2") Queue queue,@Qualifier("fanoutExchange-queueB") FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }
    /////////////////////// 一对多（全匹配） - end //////////////////////////


    //定义队列
    @Bean(name="queueDirectA")
    public Queue queueDirectA() {
        return new Queue("queueDirectA",false);
    }
    //定义队列
    @Bean(name="queueDirectB")
    public Queue queueDirectB() {
        return new Queue("queueDirectB",false);
    }
    //定义一个direct类型的交换机
    @Bean(name="directExchange-queueDirect")
    DirectExchange directExchange(){
        return new DirectExchange("directExchange-queueDirect");
    }

    @Bean
    Binding bindingDirectA(@Qualifier("queueDirectA") Queue queue,@Qualifier("directExchange-queueDirect") DirectExchange directExchange) {
        return BindingBuilder.bind(queue).to(directExchange).with("direct-liqihua");
    }
    @Bean
    Binding bindingDirectB(@Qualifier("queueDirectB") Queue queue,@Qualifier("directExchange-queueDirect") DirectExchange directExchange) {
        return BindingBuilder.bind(queue).to(directExchange).with("direct-liqihua");
    }




    //定义一条叫queueC1的消息队列


    @Bean(name="queueC1")
    public Queue queueC1() {
        return new Queue("queueC1");
    }
    //定义一条叫queueC1的消息队列


    @Bean(name="queueC2")
    public Queue queueC2() {
        return new Queue("queueC2");
    }
    //定义一条叫queueC3的消息队列


    @Bean(name="queueC3")
    public Queue queueC3() {
        return new Queue("queueC3");
    }
    //定义一个topic类型的交换机


    @Bean(name="topicExchange-queueC")
    public TopicExchange topicExchange() {
        return new TopicExchange("topicExchange-queueC");
    }
    //把队列queueC1绑定到交换机topicExchange-queueC，订阅路由键值aa.ERROR


    @Bean
    Binding bindingExchangeC1(@Qualifier("queueC1") Queue queue,@Qualifier("topicExchange-queueC") TopicExchange topicExchange) {
        //with()函数匹配路由键值，表示queueC1接收发给键值aa.ERROR的消息
        return BindingBuilder.bind(queue).to(topicExchange).with("aa.ERROR");
    }
    //把队列queueC2绑定到交换机topicExchange-queueC，订阅路由键值aa.INFO


    @Bean
    Binding bindingExchangeC2(@Qualifier("queueC2") Queue queue,@Qualifier("topicExchange-queueC") TopicExchange topicExchange) {
        //with()函数匹配路由键值，表示queueC2接收发给键值aa.INFO的消息
        return BindingBuilder.bind(queue).to(topicExchange).with("aa.INFO");
    }
    //把队列queueC3绑定到交换机topicExchange-queueC，订阅路由键值 aa. 所有消息


    @Bean
    Binding bindingExchangeC3(@Qualifier("queueC3") Queue queue,@Qualifier("topicExchange-queueC") TopicExchange topicExchange) {
        //with()函数匹配路由键值，表示queueC3接收发给aa.xxxx的消息
        return BindingBuilder.bind(queue).to(topicExchange).with("aa.#");//*表示一个词,#表示零个或多个词
    }
    /////////////////////// 一对多（订阅匹配） - end //////////////////////////



    //定义一条叫queueD的消息队列


    @Bean
    public Queue queueD() {
        return new Queue("queueD",true);
    }
    //////////////////////// 点对点（同步获取返回值） - end //////////////////////////


    //定义一条叫queueE的消息队列


    @Bean
    public Queue queueE() {
        return new Queue("queueE",true);
    }
    //////////////////////// 点对点（同步获取返回值，传递对象） - end //////////////////////////




*/




}
