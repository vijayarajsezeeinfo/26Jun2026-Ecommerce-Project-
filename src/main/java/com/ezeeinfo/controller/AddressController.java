package com.ezeeinfo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ezeeinfo.controller.io.AddressIO;
import com.ezeeinfo.controller.io.NamespaceIO;
import com.ezeeinfo.controller.io.UserIOResponse;
import com.ezeeinfo.dao.UserDAO;
import com.ezeeinfo.dto.AddressDTO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.dto.UserDTO;
import com.ezeeinfo.service.AddressService;

@RestController
@RequestMapping("/address")
public class AddressController {

	@Autowired
	AddressService addressService;
	@Autowired
	UserController userController;
	@Autowired
	NamespaceController namespaceController;
	@Autowired
	UserDAO userDAO;

	private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

	@RequestMapping(value = "/{namespaceCode}", method = RequestMethod.GET)
	public List<AddressIO> getAllAddresses(@PathVariable("namespaceCode") String namespaceCode) {
		LOG.info("{}", addressService.getAllAddresses(namespaceCode).stream().map(dto -> addressDTOToIO(dto)).toList());
		return addressService.getAllAddresses(namespaceCode).stream().map(dto -> addressDTOToIO(dto)).toList();
	}

	@RequestMapping(value = "/code/{code}", method = RequestMethod.GET)
	public AddressIO getAddressByCode(@PathVariable("code") String code) {
		LOG.info("{}", addressDTOToIO(addressService.getAddressByCode(code)));
		return addressDTOToIO(addressService.getAddressByCode(code));
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public AddressIO update(@RequestBody AddressIO addressIO) {
		LOG.info("entered AddressController.update");
		LOG.info("Updated by : {}", addressIO);
		AddressDTO addressDTO = addressService.update(addressIOToDTO(addressIO));
		return addressDTOToIO(addressDTO);
	}

	public AddressIO addressDTOToIO(AddressDTO addressDTO) {
		LOG.info("{}", addressDTO);
		UserIOResponse userIO = userController.userDTOToIO(addressDTO.getUser());
		NamespaceIO namespaceIO = namespaceController.namespaceDTOToIO(addressDTO.getNamespace());
		AddressIO addressIO = new AddressIO();
		addressIO.setCode(addressDTO.getCode());
		addressIO.setDoorNo(addressDTO.getDoorNo());
		addressIO.setStreet(addressDTO.getStreet());
		addressIO.setPlace(addressDTO.getPlace());
		addressIO.setCity(addressDTO.getCity());
		addressIO.setState(addressDTO.getState());
		addressIO.setCountry(addressDTO.getCountry());
		addressIO.setPincode(addressDTO.getPincode());
		addressIO.setUser(userIO);
		addressIO.setNamespace(namespaceIO);
		addressIO.setActiveFlag(addressDTO.getActiveFlag());
		LOG.info("{}", addressIO);
		return addressIO;
	}

	public AddressDTO addressIOToDTO(AddressIO addressIO) {
		LOG.info("{}", addressIO);
		UserDTO userDTO = userDAO.getUserByCode(addressIO.getUser().getCode());
		NamespaceDTO namespaceDTO = namespaceController.namespaceIOToDTO(addressIO.getNamespace());
		AddressDTO addressDTO = new AddressDTO();
		addressDTO.setCode(addressIO.getCode());
		addressDTO.setDoorNo(addressIO.getDoorNo());
		addressDTO.setStreet(addressIO.getStreet());
		addressDTO.setPlace(addressIO.getPlace());
		addressDTO.setCity(addressIO.getCity());
		addressDTO.setState(addressIO.getState());
		addressDTO.setCountry(addressIO.getCountry());
		addressDTO.setPincode(addressIO.getPincode());
		addressDTO.setUser(userDTO);
		addressDTO.setNamespace(namespaceDTO);
		addressDTO.setActiveFlag(addressIO.getActiveFlag());
		LOG.info("{}", addressDTO);
		return addressDTO;
	}
}
