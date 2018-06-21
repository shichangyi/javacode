package com.ec.study.demo1.nio;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class UserServer {
	private Map<Integer, User> data = new ConcurrentHashMap<Integer, User>();

	public UserServer() {
		init();
	}

	public void init() {
		for (int id = 0; id < 1000000; id++) {
			User user = new User();
			user.setId(id);
			user.setName("name" + id);
			data.put(id, user);
		}

	}

	/**
	 * 查询用户是否存在
	 * @param id
	 * @return
	 */
	public boolean exists(Integer id) {
		User user = data.get(id);
		return user == null ? false : true;
	}

	public User findById(Integer id) {
		return data.get(id);
	}

}
