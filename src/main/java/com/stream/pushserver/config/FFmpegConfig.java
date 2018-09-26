package com.stream.pushserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="ffmpgeg")
public class FFmpegConfig {
	private String path;
	private String debug;
	private Integer size;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isDebug() {
		return debug.equals("true");
	}

	public void setDebug(String debug) {
		this.debug = debug;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return "FFmpegConfig [path=" + path + ", debug=" + debug + ", size=" + size + "]";
	}
}
