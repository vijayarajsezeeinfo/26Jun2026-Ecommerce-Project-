package com.ezeeinfo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ezeeinfo.controller.io.NamespaceIO;
import com.ezeeinfo.controller.io.OrderIO;
import com.ezeeinfo.controller.io.OrderItemIO;
import com.ezeeinfo.controller.io.OrderRequestIO;
import com.ezeeinfo.controller.io.PaymentIO;
import com.ezeeinfo.controller.io.ProductIO;
import com.ezeeinfo.controller.io.UserIOResponse;
import com.ezeeinfo.dao.NamespaceDAO;
import com.ezeeinfo.dao.UserDAO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.dto.OrderDTO;
import com.ezeeinfo.dto.OrderItemDTO;
import com.ezeeinfo.dto.OrderRequestDTO;
import com.ezeeinfo.dto.PaymentDTO;
import com.ezeeinfo.dto.ProductDTO;
import com.ezeeinfo.dto.UserDTO;
import com.ezeeinfo.service.OrderRequestService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderRequestController {

	@Autowired
	OrderRequestService orderRequestService;
	@Autowired
	UserDAO userDAO;
	@Autowired
	UserController userController;
	@Autowired
	NamespaceController namespaceController;
	@Autowired
	NamespaceDAO namespaceDAO;
	@Autowired
	ProductController productController;

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public OrderRequestIO update(@RequestBody OrderRequestIO orderRequestIO) {
		log.info("OrderRequestIO : {}", orderRequestIO);
		OrderRequestDTO dto = orderRequestService.update(orIOToDTO(orderRequestIO));
		log.info("OrderRequest DTO : {}", dto);
		return orDTOToIO(dto);
	}

	@RequestMapping(value = "/code/{code}", method = RequestMethod.GET)
	public OrderRequestIO getOrderByCode(@PathVariable("code") String code) {
		return orDTOToIO(orderRequestService.getOrderByCode(code));
	}

	@RequestMapping(value = "/{namespaceCode}", method = RequestMethod.GET)
	public List<OrderRequestIO> getAllOrders(@PathVariable("namespaceCode") String namespaceCode) {
		return orderRequestService.getAllOrders(namespaceCode).stream().map(dto -> orDTOToIO(dto)).toList();
	}

	public OrderIO orderDTOToIO(OrderDTO orderDTO) {
		UserIOResponse userIO = userController.userDTOToIO(userDAO.getUserByCode(orderDTO.getUser().getCode()));
		NamespaceIO namespaceIO = namespaceController.namespaceDTOToIO(namespaceDAO.getNamespaceByCode(orderDTO.getNamespace().getCode()));
		OrderIO orderIO = new OrderIO();
		orderIO.setCode(orderDTO.getCode());
		orderIO.setUser(userIO);
		orderIO.setNamespace(namespaceIO);
		orderIO.setOrderStatus(orderDTO.getOrderStatus());
		orderIO.setTotalAmount(orderDTO.getTotalAmount());
		orderIO.setOrderDate(orderDTO.getOrderDate());
		orderIO.setActiveFlag(orderDTO.getActiveFlag());
		return orderIO;
	}

	public OrderDTO orderIOToDTO(OrderIO orderIO) {
		UserDTO userDTO = userDAO.getUserByCode(orderIO.getUser().getCode());
		NamespaceDTO namespaceDTO = namespaceDAO.getNamespaceByCode(orderIO.getNamespace().getCode());
		OrderDTO orderDTO = new OrderDTO();
		orderDTO.setCode(orderIO.getCode());
		orderDTO.setUser(userDTO);
		orderDTO.setNamespace(namespaceDTO);
		orderDTO.setOrderStatus(orderIO.getOrderStatus());
		orderDTO.setTotalAmount(orderIO.getTotalAmount());
		orderDTO.setOrderDate(orderIO.getOrderDate());
		orderDTO.setActiveFlag(orderIO.getActiveFlag());

		return orderDTO;
	}

	public PaymentIO paymentDTOToIO(PaymentDTO paymentDTO) {
		OrderIO orderIO = orderDTOToIO(paymentDTO.getOrder());
		NamespaceIO namespaceIO = namespaceController.namespaceDTOToIO(paymentDTO.getNamespace());
		PaymentIO paymentIO = new PaymentIO();
		paymentIO.setCode(paymentDTO.getCode());
		paymentIO.setOrder(orderIO);
		paymentIO.setPaymentMode(paymentDTO.getPaymentMode());
		paymentIO.setTotalAmountToPay(paymentDTO.getTotalAmountToPay());
		paymentIO.setPaidAmount(paymentDTO.getPaidAmount());
		paymentIO.setBalanceAmount(paymentDTO.getBalanceAmount());
		paymentIO.setBillingStatus(paymentDTO.getBillingStatus());
		paymentIO.setTransactionId(paymentDTO.getTransactionId());
		paymentIO.setRemarks(paymentDTO.getRemarks());
		paymentIO.setNamespace(namespaceIO);
		paymentIO.setActiveFlag(paymentDTO.getActiveFlag());
		return paymentIO;
	}

	public PaymentDTO paymentIOToDTO(PaymentIO paymentIO) {
		OrderDTO orderDTO = orderIOToDTO(paymentIO.getOrder());
		NamespaceDTO namespaceDTO = namespaceController.namespaceIOToDTO(paymentIO.getNamespace());
		PaymentDTO paymentDTO = new PaymentDTO();
		paymentDTO.setCode(paymentIO.getCode());
		paymentDTO.setOrder(orderDTO);
		paymentDTO.setPaymentMode(paymentIO.getPaymentMode());
		paymentDTO.setTotalAmountToPay(paymentIO.getTotalAmountToPay());
		paymentDTO.setPaidAmount(paymentIO.getPaidAmount());
		paymentDTO.setBalanceAmount(paymentIO.getBalanceAmount());
		paymentDTO.setBillingStatus(paymentIO.getBillingStatus());
		paymentDTO.setTransactionId(paymentIO.getTransactionId());
		paymentDTO.setRemarks(paymentIO.getRemarks());
		paymentDTO.setNamespace(namespaceDTO);
		paymentDTO.setActiveFlag(paymentIO.getActiveFlag());
		return paymentDTO;
	}

	public OrderItemIO oiDTOToIO(OrderItemDTO dto) {
		OrderIO orderIO = orderDTOToIO(dto.getOrder());
		ProductIO productIO = productController.productDTOToIO(dto.getProduct());
		NamespaceIO namespaceIO = namespaceController.namespaceDTOToIO(dto.getNamespace());
		OrderItemIO io = new OrderItemIO();
		io.setCode(dto.getCode());
		io.setActiveFlag(dto.getActiveFlag());
		io.setOrder(orderIO);
		io.setProduct(productIO);
		io.setQuantity(dto.getQuantity());
		io.setPrice(dto.getPrice());
		io.setNamespace(namespaceIO);
		return io;
	}

	public OrderItemDTO ioIOToDTO(OrderItemIO io) {
		OrderDTO orderDTO = orderIOToDTO(io.getOrder());
		ProductDTO productDTO = productController.productIOToDTO(io.getProduct());
		NamespaceDTO namespaceDTO = namespaceController.namespaceIOToDTO(io.getNamespace());
		OrderItemDTO dto = new OrderItemDTO();
		dto.setCode(io.getCode());
		dto.setActiveFlag(io.getActiveFlag());
		dto.setOrder(orderDTO);
		dto.setProduct(productDTO);
		dto.setQuantity(io.getQuantity());
		dto.setPrice(io.getPrice());
		dto.setNamespace(namespaceDTO);
		return dto;
	}

	public OrderRequestIO orDTOToIO(OrderRequestDTO dto) {
		OrderIO orderIO = orderDTOToIO(dto.getOrder());
		List<OrderItemIO> orderItemsList = dto.getOrderItems().stream().map(d -> oiDTOToIO(d)).toList();
		PaymentIO paymentIO = paymentDTOToIO(dto.getPayment());
		OrderRequestIO io = new OrderRequestIO();
		io.setOrder(orderIO);
		io.setOrderItems(orderItemsList);
		io.setPayment(paymentIO);
		return io;
	}

	public OrderRequestDTO orIOToDTO(OrderRequestIO io) {
		OrderDTO orderDTO = orderIOToDTO(io.getOrder());
		List<OrderItemDTO> orderItemsList = io.getOrderItems().stream().map(i -> ioIOToDTO(i)).toList();
		PaymentDTO paymentDTO = paymentIOToDTO(io.getPayment());
		OrderRequestDTO dto = new OrderRequestDTO();
		dto.setOrder(orderDTO);
		dto.setOrderItems(orderItemsList);
		dto.setPayment(paymentDTO);
		return dto;
	}
}
