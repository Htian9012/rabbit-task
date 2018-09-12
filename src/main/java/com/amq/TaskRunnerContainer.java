package com.amq;

import com.amq.base.TaskData;
import com.amq.base.TaskRunner;
import com.amq.conf.TaskMetaInfoManager;
import com.runner.TestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * 在此处接受MQ信息，并进行处理。
 *
 * @author
 */
public class TaskRunnerContainer {

	private static final Logger log = LoggerFactory.getLogger(TaskRunnerContainer.class);

	private ApplicationContext context;

	/**
	 * TaskScheduler
	 */
	private TaskScheduler taskScheduler;

	/**
	 * 执行任务
	 * @param taskData
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public TaskData process(TaskData taskData) throws Exception{
		try{
			TaskRunner<?, ?> taskRunner = TaskMetaInfoManager.getRunner(taskData.getTaskClass());
			taskData.setResultData(taskRunner.runTask(taskData));
		}catch (Exception e){
			throw new RuntimeException(e);
		}finally {
			log.info("complete~");
		}
		return taskData;
	}


	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

}