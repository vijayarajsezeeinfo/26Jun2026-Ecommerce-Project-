package com.ezeeinfo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ezeeinfo.dao.CategoryDAO;
import com.ezeeinfo.dao.UserDAO;
import com.ezeeinfo.dto.CategoryDTO;
import com.ezeeinfo.service.CategoryService;
import com.ezeeinfo.util.SecurityUtil;

@Service
public class CategoryServicezimpl implements CategoryService {

	@Autowired
	CategoryDAO categoryDAO;
	@Autowired
	UserDAO userDAO;

	@Override
	public List<CategoryDTO> getAllCategories(String namespaceCode) {
		// TODO Auto-generated method stub
		return categoryDAO.getAllCategories(namespaceCode);
	}

	@Override
	public CategoryDTO getCategoryByCode(String code) {
		// TODO Auto-generated method stub
		return categoryDAO.getCategoryByCode(code);
	}

	@Override
	public CategoryDTO update(CategoryDTO categoryDTO) {
		// TODO Auto-generated method stub
		categoryDTO.setUpdatedBy(userDAO.getUser(SecurityUtil.getUserId()));
		return categoryDAO.update(categoryDTO);
	}

}
