package com.ezeeinfo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ezeeinfo.controller.io.NamespaceIO;
import com.ezeeinfo.controller.io.UserIO;
import com.ezeeinfo.controller.io.UserIOResponse;
import com.ezeeinfo.dao.NamespaceDAO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.dto.UserDTO;
import com.ezeeinfo.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserService userService;
	@Autowired
	NamespaceDAO namespaceDAO;
	@Autowired
	NamespaceController namespaceController;

	@RequestMapping(value = "/{namespaceCode}", method = RequestMethod.GET)
	public List<UserIOResponse> getAllUsers(@PathVariable("namespaceCode") String namespaceCode) {
		return userService.getAllUsers(namespaceCode).stream().map(dto -> userDTOToIO(dto)).toList();
	}

	@RequestMapping(value = "/code/{code}", method = RequestMethod.GET)
	public UserIOResponse getUserByCode(@PathVariable("code") String code) {
		return userDTOToIO(userService.getUserByCode(code));
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public UserIOResponse update(@RequestBody UserIO userIO) {
		UserDTO userDTO = userService.update(userIOToDTO(userIO));
		return userDTOToIO(userDTO);
	}

	public UserIOResponse userDTOToIO(UserDTO userDTO) {
		NamespaceIO namespaceIO = namespaceController.namespaceDTOToIO(namespaceDAO.getNamespaceByCode(userDTO.getNamespace().getCode()));

		UserIOResponse userIOResponse = new UserIOResponse();
		userIOResponse.setCode(userDTO.getCode());
		userIOResponse.setUsername(userDTO.getUsername());
		userIOResponse.setEmail(userDTO.getEmail());
		userIOResponse.setMobile(userDTO.getMobile());
		userIOResponse.setRole(userDTO.getRole());
		userIOResponse.setNamespace(namespaceIO);
		userIOResponse.setActiveFlag(userDTO.getActiveFlag());

		return userIOResponse;
	}

	public UserDTO userIOToDTO(UserIO userIO) {
		NamespaceDTO namespaceDTO = namespaceDAO.getNamespaceByCode(userIO.getNamespace().getCode());

		UserDTO userDTO = new UserDTO();
		userDTO.setCode(userIO.getCode());
		userDTO.setUsername(userIO.getUsername());
		userDTO.setPassword(userIO.getPassword());
		userDTO.setEmail(userIO.getEmail());
		userDTO.setMobile(userIO.getMobile());
		userDTO.setRole(userIO.getRole());
		userDTO.setNamespace(namespaceDTO);
		userDTO.setActiveFlag(userIO.getActiveFlag());

		return userDTO;
	}
}
