package com.ezeeinfo.service;

import java.util.List;

import com.ezeeinfo.dto.OrderRequestDTO;

public interface OrderRequestService {

	List<OrderRequestDTO> getAllOrders(String namespaceCode);

	OrderRequestDTO getOrderByCode(String code);

	OrderRequestDTO update(OrderRequestDTO orderRequestDTO);
}
