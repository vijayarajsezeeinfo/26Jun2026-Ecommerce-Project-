package com.ezeeinfo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ezeeinfo.dao.ProductInventoryDAO;
import com.ezeeinfo.dao.UserDAO;
import com.ezeeinfo.dto.ProductInventoryDTO;
import com.ezeeinfo.service.ProductInventoryService;
import com.ezeeinfo.util.SecurityUtil;

@Service
public class ProductInventoryServiceImpl implements ProductInventoryService {
	@Autowired
	ProductInventoryDAO productInventoryDAO;
	@Autowired
	UserDAO userDAO;

	@Override
	public List<ProductInventoryDTO> getAllProductInventories(String namespaceCode) {
		// TODO Auto-generated method stub
		return productInventoryDAO.getAllProductInventories(namespaceCode);
	}

	@Override
	public ProductInventoryDTO getProductInventoryByCode(String code) {
		// TODO Auto-generated method stub
		return productInventoryDAO.getProductInventoryByCode(code);
	}

	@Override
	public ProductInventoryDTO update(ProductInventoryDTO productInventoryDTO) {
		// TODO Auto-generated method stub
		productInventoryDTO.setUpdatedBy(userDAO.getUser(SecurityUtil.getUserId()));
		return productInventoryDAO.update(productInventoryDTO);
	}

}
