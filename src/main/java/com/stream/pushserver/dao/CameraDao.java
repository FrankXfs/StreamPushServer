package com.stream.pushserver.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stream.pushserver.entity.Camera;


public interface CameraDao extends JpaRepository<Camera, Integer> {
	public List<Camera> findAll();

    Optional<Camera> findOneById(Integer id);
    Optional<Camera> findOneByDevicenum(String devicenum);

}
