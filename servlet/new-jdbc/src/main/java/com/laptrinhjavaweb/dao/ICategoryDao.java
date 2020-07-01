package com.laptrinhjavaweb.dao;

import java.util.List;

import com.laptrinhjavaweb.model.CategoryModel;


public interface ICategoryDao extends GenericDao<CategoryModel>{
	List<CategoryModel> FindAll();
	CategoryModel findOne(Long id);
	CategoryModel findOneByCode(String code);
}
