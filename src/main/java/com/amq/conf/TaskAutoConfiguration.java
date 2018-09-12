package com.amq.conf;

import com.amq.TaskRunnerContainer;
import com.amq.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自动装配类
 * @author
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(RabbitProperties.class)
@AutoConfigureAfter({ RabbitAutoConfiguration.class})
public class TaskAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(TaskAutoConfiguration.class);

    private TaskServerConfig serverConfig;
    /**
     * 是否已初始化配置，保证只初始化一次；
     */
    private AtomicBoolean initFlag = new AtomicBoolean(false);

    /**
     * 注入全局RabbitAdmin依赖
     * @param connectionFactory
     * @return
     */
    @Bean
    public RabbitAdmin rabbitAdmin(final ConnectionFactory connectionFactory){
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ConnectionFactory connectionFactory(final RabbitProperties properties) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(properties.getHost(),properties.getPort());
        connectionFactory.setUsername(properties.getUsername());
        connectionFactory.setPassword(properties.getPassword());
        connectionFactory.setVirtualHost("/");
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

    /**
     * 声明 taskScheduler bean
     * @param context
     * @return
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public TaskScheduler taskScheduler(final ApplicationContext context,final ConnectionFactory connectionFactory) {
        // task自定义的rabbit连接工厂
        RabbitConnectionFactoryBean factoryBean = new RabbitConnectionFactoryBean();
        // rabiit管理器
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        // rabbit模板
        RabbitTemplate rabbitTemplate = getTaskRabbitTemplate(connectionFactory);

        TaskRunnerContainer taskRunnerContainer = new TaskRunnerContainer();

        // 初始化TaskServerConfig
        serverConfig = new TaskServerConfig(context,connectionFactory,
                taskRunnerContainer,rabbitAdmin);
        // 返回TaskScheduler
        TaskScheduler taskScheduler = new TaskScheduler(rabbitTemplate, taskRunnerContainer);
        // taskRunnerContainer错误重试需要TaskScheduler
        taskRunnerContainer.setTaskScheduler(taskScheduler);
        return taskScheduler;
    }

    /**
     * ApplicationContext初始化完成或刷新后执行init方法
     */
    @EventListener(ContextRefreshedEvent.class)
    public void handleContextRefresh() {
        if (initFlag.compareAndSet(false, true)) {
            serverConfig.init();
        }
    }

    /**
     * 注册当前所有的服务。 每隔1分钟刷新一次。
     */
    @Scheduled(initialDelay = 0, fixedRate = 60000)
    public void updateConfig() {
        serverConfig.updateConfig();
    }

    /**
     * 转换器用Jackson2JsonMessageConverter，用于json转换实体类对象。
     *
     * @param connectionFactory
     * @return RabbitTemplate
     */
    private RabbitTemplate getTaskRabbitTemplate(final ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        //template.setMessageConverter(new TaskMessageConverter());
        template.setReplyTimeout(180000);
        template.afterPropertiesSet();
        return template;
    }

}
