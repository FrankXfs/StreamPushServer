package com.stream.pushserver.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stream.pushserver.dao.CameraDao;
import com.stream.pushserver.entity.Camera;
import com.stream.pushserver.entity.OperationResponse;
import com.stream.pushserver.entity.OperationResponse.ResponseStatusEnum;
import com.stream.pushserver.entity.TaskEntity;
import com.stream.pushserver.entity.TaskInfo;
import com.stream.pushserver.service.FFmpegManager;


@RestController
public class PusherRestController {
	
	@Autowired
    private FFmpegManager ffmpegManager;
	
	@Autowired
	private CameraDao cameraDao;
	
	@Value("${streamServer}")
	private String streamServer;
	
	@RequestMapping(value = "/api/push", method = RequestMethod.GET)
    public OperationResponse Push(
			    @RequestParam(value = "id"  , required = true) String id,
			    @RequestParam(value = "rtspUrl"  , required = true) String rtspUrl
			  ){
		OperationResponse resp=new OperationResponse();
		String task_id=ffmpegManager.start(id, "ffmpeg -i "+rtspUrl+" -rtsp_transport tcp -vcodec h264 -f rtsp rtsp://"+streamServer+"/"+id);
		if(task_id!=null){
			resp.setOperationStatus(ResponseStatusEnum.SUCCESS);
			resp.setOperationMessage("启动推流任务成功, task id:"+task_id+", rtspUrl:"+rtspUrl);
			
		}else{
			resp.setOperationStatus(ResponseStatusEnum.ERROR);
			resp.setOperationMessage("启动推流任务失败, rtspUrl:"+rtspUrl);
		}
		
		return resp;
		
		
	}
	
	@RequestMapping(value = "/api/push/{deviceNum}", method = RequestMethod.GET)
    public OperationResponse PushDevice(@PathVariable("deviceNum") String deviceNum){
		OperationResponse resp=new OperationResponse();
		Optional<Camera> optCam=cameraDao.findOneByDevicenum(deviceNum);
		
		if(optCam.isPresent()){
			
			String task_id=ffmpegManager.start(optCam.get().getDevicenum(), "ffmpeg -i "+optCam.get().getRtspurl()+" -rtsp_transport tcp -vcodec h264 -f rtsp rtsp://"+streamServer+"/"+optCam.get().getDevicenum());
			if(task_id!=null){
				resp.setOperationStatus(ResponseStatusEnum.SUCCESS);
				resp.setOperationMessage("启动推流任务成功, task id:"+task_id+", rtspUrl:"+optCam.get().getRtspurl());
			}else{
			
				resp.setOperationStatus(ResponseStatusEnum.ERROR);
				resp.setOperationMessage("启动推流任务失败, rtspUrl:"+optCam.get().getRtspurl());
			}
			
		}else{
			resp.setOperationStatus(ResponseStatusEnum.ERROR);
			resp.setOperationMessage("没找到设备信息, 设备编码:"+deviceNum);
			
			
		}
		
		return resp;
		
		
	}
	
	@RequestMapping(value = "/api/stop", method = RequestMethod.GET)
    public OperationResponse Stop(
			    @RequestParam(value = "id"  , required = true) String id
			  ){
		OperationResponse resp=new OperationResponse();
		boolean isSucc=ffmpegManager.stop(id);
		if(isSucc){
			resp.setOperationStatus(ResponseStatusEnum.SUCCESS);
			resp.setOperationMessage("停止推流任务成功, task id:"+id);
		}else{
			resp.setOperationStatus(ResponseStatusEnum.ERROR);
			resp.setOperationMessage("停止推流任务失败, task id:"+id);
		}
		
		return resp;
	}
	
	@RequestMapping(value = "/api/stop/{deviceNum}", method = RequestMethod.GET)
    public OperationResponse StopByDeviceNum(
    		     @PathVariable(value = "deviceNum"  , required = true) String id
			  ){
		OperationResponse resp=new OperationResponse();
		boolean isSucc=ffmpegManager.stop(id);
		if(isSucc){
			resp.setOperationStatus(ResponseStatusEnum.SUCCESS);
			resp.setOperationMessage("停止推流任务成功, task id:"+id);
		}else{
			resp.setOperationStatus(ResponseStatusEnum.ERROR);
			resp.setOperationMessage("停止推流任务失败, task id:"+id);
		}
		
		return resp;
	}
	
	@RequestMapping(value = "/api/stopall", method = RequestMethod.GET)
    public OperationResponse StopAll(){
		OperationResponse resp=new OperationResponse();
		int count=ffmpegManager.stopAll();
		if(count>0){
			resp.setOperationStatus(ResponseStatusEnum.SUCCESS);
			resp.setOperationMessage("停止了"+count+"个推流任务！");
		}else{
			resp.setOperationStatus(ResponseStatusEnum.ERROR);
			resp.setOperationMessage("没有推流任务或停止失败！");
		}
		
		return resp;
	}
	
	@RequestMapping(value = "/api/getInfo", method = RequestMethod.GET)
	public TaskInfo getInfo(
		    @RequestParam(value = "id"  , required = true) String id
		  ){
		
		TaskInfo info=new TaskInfo();

	    TaskEntity task = ffmpegManager.query(id);
	    if(task!=null){
	    	info.setId(task.getId());
	    	info.setProcess(task.getProcess().toString());
	    	info.setThread(task.getThread().toString());
	    }else return null;

	    return info;
    }
}
