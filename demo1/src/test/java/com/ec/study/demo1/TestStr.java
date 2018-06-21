package com.ec.study.demo1;

import org.junit.Test;

public class TestStr {
	
	@Test
	public void test1(){
		String args1 = "cmd=findById&id=123|cmd=findById&id=454|cmd=q";
		String args2 = "exists&id=456|";
		
		String remainStr = "";
		
		//1. 
		String args = remainStr + args1;
		String params = args.substring(0, args.lastIndexOf("|"));
		params = getParams(params);
		remainStr = args.substring(args.lastIndexOf("|"), args.length());
		System.out.println(String.format("remainStr = %s , params = %s", remainStr,params));
		
		// 2. 
		args = remainStr + args2;
		params = args.substring(0, args.lastIndexOf("|"));
		params = getParams(params);
		remainStr = args.substring(args.lastIndexOf("|"), args.length());
		System.out.println(String.format("remainStr = %s , params = %s", remainStr,params));
		
		
		
	}

	private String getParams(String params) {
		String arrs1[] = params.split("|");
		
		for(String a : arrs1){
			
		}
		
		return params;
	}
}
