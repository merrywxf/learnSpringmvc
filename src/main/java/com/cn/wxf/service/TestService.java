package com.cn.wxf.service;

import com.cn.annotation.Service;

@Service
public class TestService {

	public void insert(String name) {
		System.out.println("=====service method insert===================");
		System.out.println(name + "========================");
	}

}
