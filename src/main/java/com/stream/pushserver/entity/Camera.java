package com.stream.pushserver.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


import lombok.Data;

@Data
@Entity
@Table(name = "devices")
public class Camera {
	
	@Id
    private Integer id;
    private String  devicenum;
    private String  rtspurl;
    private Integer  status;

}
