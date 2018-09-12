package com.amq.conf;

import com.amq.base.TaskData;
import com.amq.base.TaskRunner;
import com.amq.base.TaskRunnerConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Task元信息管理器
 * @author aexon,liliang
 */
public class TaskMetaInfoManager {

	/**
	 * 运行主机配置
	 */
	static List<String> targetConfig = null;

	/**
	 * Runner任务实例缓存。
	 */
	@SuppressWarnings("rawtypes")
	static Map<String, TaskRunner> runnerMap = new HashMap<String, TaskRunner>();

	/**
	 * Runner任务配置缓存
	 */
	static ConcurrentHashMap<String, TaskRunnerConfig> runnerConfigMap = new ConcurrentHashMap<String, TaskRunnerConfig>();


	/**
	 * 获得任务运行实例。
	 *
	 * @param taskClass
	 * @return
	 */
	public static TaskRunner<?, ?> getRunner(String taskClass) {
		return runnerMap.get(taskClass);
	}

    /**
     * 获得队列任务配置
     *
     * @param configKey
     * @return
     */
	public static TaskRunnerConfig getTaskRunnerConfig(String configKey) {
        return runnerConfigMap.get(configKey);
    }

	/**
	 * 检查一个runner是否可以在本地运行。
	 * 
	 * @param taskData
	 * @return
	 */
	public static boolean checkRunnerRunLocal(TaskData<?, ?> taskData) {
		boolean exists = runnerMap.containsKey(taskData.getTaskClass());
		boolean matchTarget = false;
		if (targetConfig != null) {
			if (targetConfig.contains(taskData.getRunTarget())) {
				matchTarget = true;
			}
		}
		return exists && matchTarget;
	}

	/**
	 * 根据服务器端Queue列表，返回合适的key。
	 * 
	 * @return
	 */
	public static String getFitQueue(TaskData<?, ?> data) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(data.getTaskClass()).append("#");
		if (data.getTaskTag() != null && data.getTaskTag().length() > 0) {
			sb.append(data.getTaskTag());
		}
		sb.append("$");
		if (data.getRunTarget() != null && data.getRunTarget().length() > 0) {
			sb.append(data.getRunTarget());
		}
		String all = sb.toString();
		if (runnerConfigMap.containsKey(all)) {
			return all;
		}
		// 检测去除目标的情况
		if (!all.endsWith("$")) {
			String test = all.substring(0, all.lastIndexOf('$') + 1);
			if (runnerConfigMap.containsKey(test)) {
				return test;
			}
		}
		// 检测去除TAG的情况
		if (!all.contains("#$")) {
			String test = all.substring(0, all.indexOf('#') + 1) + all.substring(all.lastIndexOf('$'), all.length());
			if (runnerConfigMap.containsKey(test)) {
				return test;
			}
		}
		// 两个都去除的情况
		if (!all.endsWith("#$")) {
			String test = data.getTaskClass() + "#$";
			if (runnerConfigMap.containsKey(test)) {
				return test;
			}
		}
		// 最后都没匹配到，返回原始数据
		return all;
	}

	/**
	 * 获得Runner配置结合Host。
	 * 
	 * @return
	 */
	public static String getRunnerConfigKey(TaskRunnerConfig config) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(config.getTaskClass()).append("#");
		if (config.getTaskTag() != null && config.getTaskTag().length() > 0) {
			sb.append(config.getTaskTag());
		}
		sb.append("$");
		if (config.getRunTarget() != null && config.getRunTarget().length() > 0) {
			sb.append(config.getRunTarget());
		}
		return sb.toString();
	}

}
