package com.stream.pushserver.dao;


import com.stream.pushserver.entity.TaskEntity;
import com.stream.pushserver.service.TaskHandler;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 任务信息持久层实现
 * 
 * @author eguid
 * @since jdk1.7
 * @version 2016年10月29日
 */
@Component
@ConfigurationProperties(prefix="ffmpgeg")
public class TaskDaoImpl implements TaskDao {
	// 存放任务信息
	private ConcurrentMap<String, TaskEntity> map = null;
	
	@Autowired
	private TaskHandler taskHandler;
	
	private int size=1000;

	public TaskDaoImpl() {
		map = new ConcurrentHashMap<>(size);
	}

	@Override
	public TaskEntity get(String id) {
		return map.get(id);
	}

	@Override
	public Collection<TaskEntity> getAll() {
		return map.values();
	}

	@Override
	public int add(TaskEntity taskEntity) {
		String id = taskEntity.getId();
		if (id != null && !map.containsKey(id)) {
			map.put(taskEntity.getId(), taskEntity);
			if(map.get(id)!=null)
			{
				return 1;
			}
		}else if (id != null && map.containsKey(id)){
			taskHandler.stop(map.get(id).getProcess(), map.get(id).getThread());
			map.remove(id);
			map.put(taskEntity.getId(), taskEntity);
			if(map.get(id)!=null)
			{
				return 1;
			}
		}
		return 0;
	}

	@Override
	public int remove(String id) {
		if(map.remove(id) != null){
			return 1;
		};
		return 0;
	}

	@Override
	public int removeAll() {
		int size = map.size();
		try {
			map.clear();
		} catch (Exception e) {
			return 0;
		}
		return size;
	}

	@Override
	public boolean isHave(String id) {
		return map.containsKey(id);
	}

}
