package com.laptrinhjavaweb.dao.impl;

import java.util.List;

import com.laptrinhjavaweb.dao.IUserDao;
import com.laptrinhjavaweb.mapper.UserMapper;
import com.laptrinhjavaweb.model.UserModel;

public class UserDao extends AbtractDao<UserModel> implements IUserDao{

	@Override
	public UserModel FindByUserNameAndPasswordAndStatus(String userName, String password, Integer status) {
		StringBuilder sql = new StringBuilder("SELECT * FROM user ");
		sql.append(" u join role r on r.id = u.role_id");
		sql.append(" WHERE username = ? AND password = ? AND status = ?");
		
		List<UserModel>users = query(sql.toString(), new UserMapper() , userName,password ,status);
		
		return users.isEmpty() ? null : users.get(0);
	}
	
}
