package com.stream.pushserver.service.impl;



import com.stream.pushserver.config.FFmpegConfig;
//import com.hxjd.build.Entry.Video;
//import com.hxjd.build.service.VideoService;
//import com.hxjd.build.service.imp.VideoServiceImp;
//import com.hxjd.build.utils.PlugFlow;
//import com.hxjd.build.utils.SavePlayInfoUtil;
import com.stream.pushserver.dao.TaskDao;
import com.stream.pushserver.dao.TaskDaoImpl;
import com.stream.pushserver.entity.TaskEntity;
import com.stream.pushserver.service.CommandAssembly;
import com.stream.pushserver.service.FFmpegManager;
import com.stream.pushserver.service.TaskHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * FFmpeg命令操作管理器
 * 
 * @author eguid
 * @since jdk1.7
 * @version 2016年10月29日
 */
@Service
public class FFmpegManagerImpl implements FFmpegManager {
	
	@Autowired
	private TaskDao taskDao ;
	
	@Autowired
	private TaskHandler taskHandler;
	
	@Autowired
	private CommandAssembly commandAssembly;
	
	@Autowired
	private FFmpegConfig config;
	

//	public FFmpegManagerImpl() {
////		if (config == null) {
////			System.err.println("配置文件加载失败！配置文件不存在或配置错误");
////			return;
////		}
////		init(config.getSize()==null?10:config.getSize());
//	}

//	public FFmpegManagerImpl(int size) {
//		if (config == null) {
//			System.err.println("配置文件加载失败！配置文件不存在或配置错误");
//			return;
//		}
//		init(size);
//	}

	/**
	 * 初始化
	 * 
	 * @param size
	 */
//	public void init(int size) {
//		this.taskDao = new TaskDaoImpl(size);
//		this.taskHandler = new TaskHandlerImpl();
//		this.commandAssembly = new CommandAssemblyImpl();
//	}

//	public void setTaskDao(TaskDao taskDao) {
//		this.taskDao = taskDao;
//	}
//
//	public void setTaskHandler(TaskHandler taskHandler) {
//		this.taskHandler = taskHandler;
//	}
//
//	public void setCommandAssembly(CommandAssembly commandAssembly) {
//		this.commandAssembly = commandAssembly;
//	}

	@Override
	public String start(String id, String command) {
		return start(id,command,false);
	}
	@Override
	public String start(String id, String command, boolean hasPath) {
		if (id != null && command != null) {
			TaskEntity tasker = taskHandler.process(id, hasPath?command: config.getPath()+command);
			if (tasker != null) {
				int ret = taskDao.add(tasker);
				if (ret > 0) {
					return tasker.getId();
				} else {
					// 持久化信息失败，停止处理
					taskHandler.stop(tasker.getProcess(), tasker.getThread());
					if(config.isDebug())
					System.err.println("持久化失败，停止任务！");
				}
			}
		}
		return null;
	}
	@Override
	public String start(Map assembly) {
		// ffmpeg环境是否配置正确
		if (config==null) {
			System.err.println("配置未正确加载，无法执行");
			return null;
		}
		// 参数是否符合要求
		if (assembly == null || assembly.isEmpty() || !assembly.containsKey("appName")) {
			System.err.println("参数不正确，无法执行");
			return null;
		}
		String appName = (String) assembly.get("appName");
		if (appName != null && "".equals(appName.trim())) {
			System.err.println("appName不能为空");
			return null;
		}
		assembly.put("ffmpegPath", config.getPath()+"ffmpeg");
		String command = commandAssembly.assembly(assembly);
		if (command != null) {
			return start(appName, command,true);
		}

		return null;
	}

	@Override
	public boolean stop(String id) {
		if (id != null && taskDao.isHave(id)) {
			if(config.isDebug())
			System.out.println("正在停止任务：" + id);
			TaskEntity tasker = taskDao.get(id);
			if (taskHandler.stop(tasker.getProcess(), tasker.getThread())) {
				taskDao.remove(id);
				return true;
			}
		}
		System.err.println("停止任务失败！id="+id);
		return false;
	}

	@Override
	public int stopAll() {
		Collection<TaskEntity> list = taskDao.getAll();
		Iterator<TaskEntity> iter = list.iterator();
		TaskEntity tasker = null;
		int index = 0;
		while (iter.hasNext()) {
			tasker = iter.next();
			if (taskHandler.stop(tasker.getProcess(), tasker.getThread())) {
				taskDao.remove(tasker.getId());
				index++;
			}
		}
		if(config.isDebug())
		System.out.println("停止了" + index + "个任务！");
		return index;
	}

	@Override
	public TaskEntity query(String id) {
		return taskDao.get(id);
	}

	@Override
	public Collection<TaskEntity> queryAll() {
		return taskDao.getAll();
	}

	public static void main(String[] args) {
		FFmpegManager manager = new FFmpegManagerImpl();
		Map map = new HashMap();
		map.put("appName", "test1");
//		map.put("input", "rtsp://admin:admin@192.168.0.172/cam/realmonitor?channel=1&subtype=0");
		map.put("input", "rtsp://admin:hxjd2017@192.168.0.36:554/Streaming/tracks/201?starttime=20170820t090923z&endtime=20170820t095923z");
//		map.put("output", "http://192.168.0.186:8588/hls/");
		map.put("output", "rtmp://192.168.0.186:1935/live/");
		map.put("codec", "h264");
		map.put("fmt", "flv");
		map.put("fps", "25");
//		map.put("rs", "1080x720");
		map.put("rs", "640x360");
		map.put("twoPart", "1");
		// 执行任务，id就是appName，如果执行失败返回为null
		//String id = manager.start(map);
		//System.out.println(id);
		// 通过id查询
		//TaskEntity info = manager.query(id);
		//System.out.println(info);
		// 查询全部
		//Collection<TaskEntity> infoList = manager.queryAll();
		//System.out.println(infoList);
		// 停止id对应的任务
//		manager.stop(id);
//		manager.start("test1", "这里放原生的ffmpeg命令");
		manager.start("test1", "ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -rtsp_transport tcp -vcodec h264 -f rtsp rtsp://192.168.1.124/test");
//		manager.start("test1", "ffmpeg -i rtsp://admin:admin@192.168.0.172/cam/realmonitor?channel=1&subtype=0  -strict -2 -c:v libx264 -c:a aac -f hls E:\\demo\\nginx-rtmp-win32-nms\\tmp\\hls\\videoS.m3u8");
		// 停止全部任务
		
		// 通过id查询
				TaskEntity info = manager.query("test1");
				System.out.println(info);
				// 查询全部
				Collection<TaskEntity> infoList = manager.queryAll();
				System.out.println(infoList);
				
				manager.stop("test1");
//		manager.stopAll();
	}
	
}
