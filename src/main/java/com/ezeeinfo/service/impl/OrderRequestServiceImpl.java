package com.ezeeinfo.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ezeeinfo.dao.OrderDAO;
import com.ezeeinfo.dao.UserDAO;
import com.ezeeinfo.dto.OrderItemDTO;
import com.ezeeinfo.dto.OrderRequestDTO;
import com.ezeeinfo.exception.ServiceException;
import com.ezeeinfo.service.OrderRequestService;
import com.ezeeinfo.util.SecurityUtil;

@Service

public class OrderRequestServiceImpl implements OrderRequestService {

	@Autowired
	OrderDAO orderDAO;
	@Autowired
	UserDAO userDAO;
	
	private static final Logger LOG = LoggerFactory.getLogger(OrderRequestServiceImpl.class);

	@Override
	public OrderRequestDTO getOrderByCode(String code) {
		return orderDAO.getOrderByCode(code);
	}

	@Override
	public OrderRequestDTO update(OrderRequestDTO orderRequestDTO) {
		// TODO Auto-generated method stub
		LOG.info("OrderRequest DTO : {}", orderRequestDTO);

		orderRequestDTO.getOrder().setUpdatedBy(userDAO.getUser(SecurityUtil.getUserId()));
		for (OrderItemDTO item : orderRequestDTO.getOrderItems()) {
			item.setUpdatedBy(userDAO.getUser(SecurityUtil.getUserId()));
		}
		orderRequestDTO.getPayment().setUpdatedBy(userDAO.getUser(SecurityUtil.getUserId()));

		if (!orderRequestDTO.getOrder().getNamespace().getCode().equals(orderRequestDTO.getOrder().getUser().getNamespace().getCode())) {
			throw new ServiceException(" Order's namespace and User's namespace does not match");
		}
		return orderDAO.update(orderRequestDTO);
	}

	@Override
	public List<OrderRequestDTO> getAllOrders(String namespaceCode) {
		
		return orderDAO.getAllOrders(namespaceCode);
	}

}
