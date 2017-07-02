package com.cn.wxf.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cn.annotation.Autowired;
import com.cn.annotation.Controller;
import com.cn.annotation.RequestMapping;
import com.cn.wxf.service.TestService;

@Controller
@RequestMapping("/test")
public class TestController {

	@Autowired(value = "testService")
	private TestService testService;

	@RequestMapping("/insert")
	public void insert(HttpServletRequest req, HttpServletResponse res, String name) {
		System.out.println(name+"========================");
		this.testService.insert(name);
	}
}
