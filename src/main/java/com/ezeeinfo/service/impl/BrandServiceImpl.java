package com.ezeeinfo.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ezeeinfo.dao.BrandDAO;
import com.ezeeinfo.dao.UserDAO;
import com.ezeeinfo.dto.BrandDTO;
import com.ezeeinfo.service.BrandService;
import com.ezeeinfo.util.SecurityUtil;

@Service
public class BrandServiceImpl implements BrandService {
	@Autowired
	BrandDAO brandDAO;
	@Autowired
	UserDAO userDAO;
	
	private static final Logger LOG = LoggerFactory.getLogger(BrandServiceImpl.class);

	@Override
	public List<BrandDTO> getAllBrands(String namespaceCode) {
		// TODO Auto-generated method stub
		return brandDAO.getAllBrands(namespaceCode);
	}

	@Override
	public BrandDTO getBrandByCode(String code) {
		// TODO Auto-generated method stub
		return brandDAO.getBrandByCode(code);
	}

	@Override
	public BrandDTO update(BrandDTO brandDTO) {
		// TODO Auto-generated method stub
		brandDTO.setUpdatedBy(userDAO.getUser(SecurityUtil.getUserId()));
		return brandDAO.update(brandDTO);
	}

}
