package com.ec.study.demo1.nio;

import lombok.Data;

@Data
public class Args {
	private Integer id;
	private String cmd;
	public void build(String args) {
		try{String arr[] = args.split("&");
		cmd = arr[0].split("=")[1];
		id = Integer.valueOf(arr[1].split("=")[1]);
			
		}catch (Exception e) {
		}
		
	}
	
}
