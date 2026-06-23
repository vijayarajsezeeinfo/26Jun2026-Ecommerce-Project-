package com.ezeeinfo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ezeeinfo.dao.CartItemDAO;
import com.ezeeinfo.dao.UserDAO;
import com.ezeeinfo.dto.CartItemDTO;
import com.ezeeinfo.service.CartItemService;
import com.ezeeinfo.util.SecurityUtil;

@Service
public class CartItemServiceImpl implements CartItemService {

	@Autowired
    CartItemDAO cartItemDAO;
	@Autowired
	UserDAO userDAO;

	@Override
	public List<CartItemDTO> getAllCartItems(String namespaceCode) {
		return cartItemDAO.getAllCartItems(namespaceCode);
	}
	
//
//	@Override
//	public CartItemDTO getCartItemByCode(String code) {
//		return cartItemDAO.getCartItemByCode(code);
//	}

	@Override
	public CartItemDTO update(CartItemDTO cartItemDTO) {

		cartItemDTO.setUpdatedBy(userDAO.getUser(SecurityUtil.getUserId()));

		return cartItemDAO.update(cartItemDTO);
	}
}