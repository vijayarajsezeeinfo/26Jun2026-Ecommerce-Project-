package com.ezeeinfo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ezeeinfo.controller.io.CartIO;
import com.ezeeinfo.controller.io.CartItemIO;
import com.ezeeinfo.controller.io.NamespaceIO;
import com.ezeeinfo.controller.io.ProductIO;
import com.ezeeinfo.controller.io.UserIOResponse;
import com.ezeeinfo.dao.UserDAO;
import com.ezeeinfo.dto.CartDTO;
import com.ezeeinfo.dto.CartItemDTO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.dto.ProductDTO;
import com.ezeeinfo.dto.UserDTO;
import com.ezeeinfo.service.CartItemService;

@RestController
@RequestMapping("/cart-item")
public class CartItemController {

	@Autowired
	private CartItemService cartItemService;
	@Autowired
	UserController userController;
	@Autowired
	UserDAO userDAO;
	@Autowired
	NamespaceController namespaceController;
	@Autowired
	ProductController productController;

	private static final Logger LOG = LoggerFactory.getLogger(CartItemController.class);

	@RequestMapping(value = "/{namespaceCode}", method = RequestMethod.GET)
	public List<CartItemIO> getAllCartItems(@PathVariable("namespaceCode") String namespaceCode) {
		return cartItemService.getAllCartItems(namespaceCode).stream().map(dto -> cartItemDTOToIO(dto)).toList();
	}

	//
	// @RequestMapping(value = "/code/{code}", method = RequestMethod.GET)
	// public CartItemIO getCartItemByCode(@PathVariable String code) {
	// return cartItemDTOToIO(cartItemService.getCartItemByCode(code));
	// }

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public CartItemIO update(@RequestBody CartItemIO io) {
		return cartItemDTOToIO(cartItemService.update(cartItemIOToDTO(io)));
	}

	public CartItemIO cartItemDTOToIO(CartItemDTO dto) {

		LOG.info("CartItem dto : {} ", dto);

		CartIO cartIO = cartDTOToIO(dto.getCart());
		ProductIO productIO = productController.productDTOToIO(dto.getProduct());
		NamespaceIO namespaceIO = namespaceController.namespaceDTOToIO(dto.getNamespace());

		CartItemIO cartItemIO = new CartItemIO();
		cartItemIO.setCode(dto.getCode());
		cartItemIO.setCart(cartIO);
		cartItemIO.setProduct(productIO);
		cartItemIO.setQuantity(dto.getQuantity());
		cartItemIO.setNamespace(namespaceIO);
		cartItemIO.setActiveFlag(dto.getActiveFlag());
		LOG.info("CartItem io : {} ", cartItemIO);

		return cartItemIO;
	}

	public CartItemDTO cartItemIOToDTO(CartItemIO io) {
		LOG.info("CartItem io : {} ", io);

		CartDTO cartDTO = cartIOToDTO(io.getCart());
		ProductDTO productDTO = productController.productIOToDTO(io.getProduct());
		NamespaceDTO namespaceDTO = namespaceController.namespaceIOToDTO(io.getNamespace());

		CartItemDTO cartItemDTO = new CartItemDTO();
		cartItemDTO.setCode(io.getCode());
		cartItemDTO.setCart(cartDTO);
		cartItemDTO.setProduct(productDTO);
		cartItemDTO.setQuantity(io.getQuantity());
		cartItemDTO.setNamespace(namespaceDTO);
		cartItemDTO.setActiveFlag(io.getActiveFlag());
		LOG.info("CartItem dto : {} ", cartItemDTO);

		return cartItemDTO;
	}

	public CartIO cartDTOToIO(CartDTO dto) {
		CartIO io = new CartIO();
		io.setCode(dto.getCode());
		io.setActiveFlag(dto.getActiveFlag());
		UserIOResponse userIO = userController.userDTOToIO(dto.getUser());
		io.setUser(userIO);
		NamespaceIO namespaceIO = namespaceController.namespaceDTOToIO(dto.getNamespace());
		io.setNamespace(namespaceIO);
		return io;
	}

	public CartDTO cartIOToDTO(CartIO io) {
		CartDTO dto = new CartDTO();
		dto.setCode(io.getCode());
		dto.setActiveFlag(io.getActiveFlag());
		UserDTO userDTO = userDAO.getUserByCode(io.getUser().getCode());
		dto.setUser(userDTO);
		NamespaceDTO namespaceDTO = namespaceController.namespaceIOToDTO(io.getNamespace());
		dto.setNamespace(namespaceDTO);
		return dto;
	}

}