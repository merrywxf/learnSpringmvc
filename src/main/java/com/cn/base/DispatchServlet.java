package com.cn.base;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cn.annotation.Autowired;
import com.cn.annotation.Controller;
import com.cn.annotation.RequestMapping;
import com.cn.annotation.Service;

public class DispatchServlet extends HttpServlet {

	private Map<String, Object> instMap = new HashMap<String, Object>();
	private Map<String, Object> urlMap = new HashMap<String, Object>();

	public DispatchServlet() {
		super();
		try {
			this.scanPackage("com.cn.wxf");
			this.initUrlMap();
			this.initIoc();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initIoc() throws IllegalArgumentException, IllegalAccessException {
		for (Entry<String, Object> entry : instMap.entrySet()) {
			Object n = entry.getValue();
			Field[] fields = n.getClass().getDeclaredFields();
			for (Field f : fields) {
				if (f.isAnnotationPresent(Autowired.class)) {
					String value = f.getAnnotation(Autowired.class).value();
					Object service = instMap.get(value.toLowerCase());
					if (service == null) {
						service = instMap.get(f.getName().toLowerCase());
					}
					f.setAccessible(true);
					f.set(n, service);
				} else {
					continue;
				}
			}
		}
	}

	/**
	 * 做 url 到方法的映射
	 * </p>
	 * 添加到map中
	 */
	private void initUrlMap() {
		for (Entry<String, Object> entry : instMap.entrySet()) {
			Object n = entry.getValue();
			if (n.getClass().isAnnotationPresent(Controller.class)) {
				if (n.getClass().isAnnotationPresent(RequestMapping.class)) {
					String classUrl = n.getClass().getAnnotation(RequestMapping.class).value();
					// 处理类中的方法
					Method[] methods = n.getClass().getMethods();
					for (Method mt : methods) {
						if (mt.isAnnotationPresent(RequestMapping.class)) {
							String value = mt.getAnnotation(RequestMapping.class).value();
							urlMap.put(classUrl + value, mt);
						}
					}
				}
			}
		}
	}

	/**
	 * 扫描类添加到map中
	 */
	private void scanPackage(String basePackage)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String basePackages = basePackage.replace(".", "/");
		String path = this.getClass()//
				.getClassLoader()//
				.getResource("/").getPath() + basePackages;
		File file = new File(path);
		File[] files = file.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				this.scanPackage(basePackage + "." + f.getName());
			} else {
				// 扫描到类
				String classPath = basePackage + "." + f.getName().substring(0, f.getName().length() - 6);
				Class clazz = Class.forName(classPath);
				if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)) {
					String clazzName = f.getName().substring(0, f.getName().length() - 6);
					instMap.put(clazzName.toLowerCase(), clazz.newInstance());
				} else {
					continue;
				}
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.dispath(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.dispath(req, resp);
	}

	private void dispath(HttpServletRequest req, HttpServletResponse resp) {
		String url = req.getRequestURI();
		String realPath = req.getSession().getServletContext().getContextPath();
		String key = url.substring(realPath.length());
		Object object = urlMap.get(key);
		Method method = (Method) object;
		Class<?> declaringClass = method.getDeclaringClass();
		System.out.println(declaringClass.getSimpleName() + "=======declaringClass===================");
		try {
			method.invoke(instMap.get(declaringClass.getSimpleName().toLowerCase()), req, resp, "hello");
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
