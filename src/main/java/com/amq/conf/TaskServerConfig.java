package com.amq.conf;

import com.amq.TaskRunnerContainer;
import com.amq.base.TaskRunner;
import com.amq.base.TaskRunnerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据从服务器端加载的信息来配置服务和任务项。
 *
 * @author
 * @param
 */
public class TaskServerConfig {

	private static final Logger log = LoggerFactory.getLogger(TaskServerConfig.class);

	private ApplicationContext context;

	private ConnectionFactory taskConnectionFactory;

	private TaskRunnerContainer taskRunnerContainer;

	private RabbitAdmin rabbitAdmin;

	/**
	 * container缓存。key=队列名,value=container。
	 */
	private ConcurrentHashMap<String, SimpleMessageListenerContainer> runnerContainerMap = new ConcurrentHashMap<String, SimpleMessageListenerContainer>();

	/**
	 * 启动时间。
	 */
	private long startTime = System.currentTimeMillis();

	/**
	 * 上次更新配置时间，初始值必须=0，用于标识整体加载。
	 */
	private long lastUpdateTime = 0;

	/**
	 * 队列名更新时间。
	 */
	private long queueUpdateTime = 0;

	/**
	 * 从服务器端拉取数据是否成功。
	 */
	private boolean updateFlag = true;

	/**
	 * 是否首次启动
	 */
	private boolean isFirstRun = true;

	/**
	 * 默认构造器
	 * 
	 * @param taskRabbitConnectionFactory
	 * @param taskRunnerContainer
	 * @param rabbitAdmin
	 */
	public TaskServerConfig(ApplicationContext context,
                            ConnectionFactory taskRabbitConnectionFactory, TaskRunnerContainer taskRunnerContainer, RabbitAdmin rabbitAdmin) {
		this.context = context;
		this.taskConnectionFactory = taskRabbitConnectionFactory;
		this.taskRunnerContainer = taskRunnerContainer;
		this.rabbitAdmin = rabbitAdmin;
	}

	/**
	 * 启动时执行一次。
	 */
	public void init() {
		initRunnerMap();
	}


	/**
	 * 初始化runnerMap。 因为getBeansOfType出来的key不对。
	 */
	private void initRunnerMap() {
		// 设置当前主机上所有的TaskCroner
		Map<String, TaskRunner> runnerInstanceMap = context.getBeansOfType(TaskRunner.class);
		for (Entry<String, TaskRunner> kv : runnerInstanceMap.entrySet()) {
			// 拿到任务类名
			TaskRunner tr = kv.getValue();
			String taskClass = tr.getClass().getName();
			log.info("缓存 task class,{}",taskClass);
			TaskMetaInfoManager.runnerMap.put(taskClass, tr);
		}
	}

	/**
	 * 注册单个任务
	 *
	 * @param runnerConfig
	 */
	private void registerRunner(TaskRunnerConfig runnerConfig) {
		String queueName = TaskMetaInfoManager.getRunnerConfigKey(runnerConfig);
		SimpleMessageListenerContainer container = runnerContainerMap.get(queueName);
		if (container == null) {
			if (runnerConfig.getState() != 1) {
				log.warn("TaskRunner:[{}]状态为暂停，不进行注册。。。", queueName);
				return;
			}
			if (log.isDebugEnabled()) {
				log.debug("TaskRunner:[{}]正在注册并启动监听...", queueName);
			}
			try {
				synchronized (runnerContainerMap) {
					// 定义队列
					rabbitAdmin.declareQueue(new Queue(queueName, true));
					// 定义交换机
					rabbitAdmin.declareExchange(ExchangeBuilder.directExchange(queueName).durable(true).build());
					// 绑定
					rabbitAdmin
							.declareBinding(new Binding(queueName, DestinationType.QUEUE, queueName, queueName, null));
					// 启动任务监听器
					container = new SimpleMessageListenerContainer();
					container.setAutoStartup(false);
					container.setTaskExecutor(new SimpleAsyncTaskExecutor(queueName));
					// 提高启动consumer速度。
                    container.setStartConsumerMinInterval(1000);
					container.setConsecutiveActiveTrigger(3);
                    container.setStopConsumerMinInterval(20000);
                    container.setConsecutiveIdleTrigger(3);
					container.setMaxConcurrentConsumers(runnerConfig.getConsumerNum());
                    container.setConcurrentConsumers((int) Math.ceil(runnerConfig.getConsumerNum() * 0.1f));
                    container.setPrefetchCount(runnerConfig.getPrefetchNum());
					container.setConnectionFactory(taskConnectionFactory);
					container.setAcknowledgeMode(AcknowledgeMode.AUTO);
					container.setQueueNames(queueName);
					MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(taskRunnerContainer, "process");
					// listenerAdapter.setReplyPostProcessor(new
					// GZipPostProcessor());
					//listenerAdapter.setMessageConverter(new TaskMessageConverter());
					container.setMessageListener(listenerAdapter);
					//container.setMessageConverter(new TaskMessageConverter());
					// container.setAfterReceivePostProcessors(new
					// GUnzipPostProcessor());
					container.setAutoStartup(true);
					container.afterPropertiesSet();
					container.start();
					runnerContainerMap.putIfAbsent(queueName, container);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			if (runnerConfig.getState() == 1) {
				try {
                    container.setMaxConcurrentConsumers(runnerConfig.getConsumerNum());
                    container.setConcurrentConsumers((int) Math.ceil(runnerConfig.getConsumerNum() * 0.1f));
                    container.setPrefetchCount(runnerConfig.getPrefetchNum());
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else {
				runnerContainerMap.remove(queueName);
				container.shutdown();
				container.stop();
			}
		}
	}

	/**
	 * 停止所有的任务。
	 */
	public void stopAllTaskRunner() {
		for (SimpleMessageListenerContainer container : runnerContainerMap.values()) {
			container.shutdown();
			container.stop();
		}
	}

	public void updateConfig() {
		// 第一次执行初始化操作
		if (isFirstRun) {
			isFirstRun = false;
			String queueName = "ququeName-test";//队列名
			String exName = "exName-test";//交换机名
			String routeKey = "routeKey-test";//路由key名
			Queue queue = new Queue(queueName, false);//durable->false：非持久化
			rabbitAdmin.declareQueue(queue);//声明队列
			//创建交换机，设置交换机非持久的，不使用时自动删除的
			DirectExchange ex = new DirectExchange(exName, false, true);
			//声明交换机，如果本来存在个名字相同其他属性不同的交换机，那么再次声明会报异常，这里做删除旧的，声明新的处理
			try {
				rabbitAdmin.declareExchange(ex);
			} catch (Exception e) {
				rabbitAdmin.deleteExchange(exName);
				rabbitAdmin.declareExchange(ex);
			}
			//把队列帮定到交换机并且指定队列接收指定路由key名的数据
			rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(ex).with(routeKey));
			// 启动任务监听器
			SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
			container.setAutoStartup(false);
			container.setTaskExecutor(new SimpleAsyncTaskExecutor(queueName));
			// 提高启动consumer速度。
			container.setStartConsumerMinInterval(1000);
			container.setConsecutiveActiveTrigger(3);
			container.setStopConsumerMinInterval(20000);
			container.setConsecutiveIdleTrigger(3);
			container.setConnectionFactory(taskConnectionFactory);
			container.setAcknowledgeMode(AcknowledgeMode.AUTO);
			container.setQueueNames(queueName);
			MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(new TaskRunnerContainer(), "process");
			//listenerAdapter.addQueueOrTagToMethodName(queueName,"process");
			//TaskMessageConverter taskMessageConverter = new TaskMessageConverter();
			//listenerAdapter.setMessageConverter(taskMessageConverter);
			container.setMessageListener(listenerAdapter);
			//container.setMessageConverter(taskMessageConverter);
			container.setAutoStartup(true);
			container.afterPropertiesSet();
			container.start();
			log.info("任务监听器启动完成");
		}
	}
}