package com.stream.pushserver.service.impl;

import com.stream.pushserver.config.FFmpegConfig;
import com.stream.pushserver.entity.TaskEntity;
import com.stream.pushserver.service.TaskHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


/**
 * 任务处理实现
 * @author eguid
 * @since jdk1.7
 * @version 2016年10月29日
 */
@Service
public class TaskHandlerImpl implements TaskHandler {
	private Runtime runtime = null;
	
	@Autowired
	private FFmpegConfig config;

	@Override
	public TaskEntity process(String id, String command) {
		Process process = null;
		OutHandler outHandler = null;
		TaskEntity tasker = null;
		try {
			if (runtime == null) {
				runtime = Runtime.getRuntime();
			}
			if(config.isDebug())
			System.out.println("执行命令："+command);
			
			process = runtime.exec(command);// 执行本地命令获取任务主进程
			outHandler = new OutHandler(process.getErrorStream(), id,config);
			outHandler.start();
			//tasker = new TaskEntity(id, process, outHandler);
			tasker = new TaskEntity();
			tasker.setId(id);
			tasker.setProcess(process);
			tasker.setThread(outHandler);
			
		} catch (IOException e) {
			if(config.isDebug())
			System.err.println("执行命令失败！正在停止进程和输出线程...");
			stop(outHandler);
			stop(process);
			// 出现异常说明开启失败，返回null
			return null;
		}
		return tasker;
	}

	@Override
	public boolean stop(Process process) {
		if (process != null) {
			process.destroy();
			return true;
		}
		return false;
	}

	@Override
	public boolean stop(Thread outHandler) {
		if (outHandler != null && outHandler.isAlive()) {
			outHandler.stop();
			outHandler.destroy();
			return true;
		}
		return false;
	}

	@Override
	public boolean stop(Process process, Thread thread) {
		boolean ret;
		ret=stop(thread);
		ret=stop(process);
		return ret;
	}
}

/**
 * 
 * @author eguid
 *
 */
class OutHandler extends Thread {
	/**
	 * 控制状态
	 */
	private volatile boolean desstatus = true;

	/**
	 * 读取输出流
	 */
	private BufferedReader br = null;

	/**
	 * 输出类型
	 */
	private String type = null;
	
	
	private FFmpegConfig config;

	public OutHandler(InputStream is, String type,FFmpegConfig config) {
		br = new BufferedReader(new InputStreamReader(is));
		this.type = type;
		this.config=config;
	}

	/**
	 * 重写线程销毁方法，安全的关闭线程
	 */
	@Override
	public void destroy() {
		setDesStatus(false);
	}

	public void setDesStatus(boolean desStatus) {
		this.desstatus = desStatus;
	}

	/**
	 * 执行输出线程
	 */
	@Override
	public void run() {
		String msg = null;
		int index = 0;
		int errorIndex = 0;
		int status = 10;
		try {
			if(config.isDebug()){
			System.out.println(type + "开始推流！");
			while (desstatus && (msg = br.readLine()) != null) {
				System.out.println("msg的信息是："+msg);
				if (msg.indexOf("[rtsp") != -1) {
					System.out.println("接收" + status + "个数据包" + msg);
					System.out.println(type + "，网络异常丢包，丢包次数:" + errorIndex++ + "，消息体：" + msg);
					status = 10;
					index = 0;
				}

				if (index % status == 0) {
					System.out.println("接收" + status + "个数据包" + msg);
					status *= 2;
				}
				index++;
				System.out.println("......."+index);
			}
			}else{
				Thread.yield();
			}
		} catch (IOException e) {
			System.out.println("发生内部异常错误，自动关闭[" + this.getId() + "]线程");
			destroy();
		} finally {
			if (this.isAlive()) {
				destroy();
			}
		}
	}
}