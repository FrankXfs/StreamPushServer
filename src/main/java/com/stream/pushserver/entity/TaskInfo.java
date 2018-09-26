package com.stream.pushserver.entity;

import lombok.Data;

@Data
public class TaskInfo {
	
	private  String id;//任务id
	private  String process;//任务主进程
	private  String thread;//任务输出线程

}
