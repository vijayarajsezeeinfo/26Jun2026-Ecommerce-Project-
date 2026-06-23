package com.ezeeinfo.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ezeeinfo.dao.AddressDAO;
import com.ezeeinfo.dao.UserDAO;
import com.ezeeinfo.dto.AddressDTO;
import com.ezeeinfo.service.AddressService;
import com.ezeeinfo.util.SecurityUtil;

@Service

public class AddressServiceImpl implements AddressService {
	@Autowired
	AddressDAO addressDAO;
	@Autowired
	UserDAO userDAO;

	private static final Logger LOG = LoggerFactory.getLogger(AddressServiceImpl.class);

	@Override
	public List<AddressDTO> getAllAddresses(String namespaceCode) {
		// TODO Auto-generated method stub
		LOG.info("{}", addressDAO.getAllAddresses(namespaceCode));
		return addressDAO.getAllAddresses(namespaceCode);
	}

	@Override
	public AddressDTO getAddressByCode(String code) {
		// TODO Auto-generated method stub
		LOG.info("{}", addressDAO.getAddressByCode(code));
		return addressDAO.getAddressByCode(code);
	}

	@Override
	public AddressDTO update(AddressDTO addressDTO) {
		// TODO Auto-generated method stub
		LOG.info("entered AddressServiceImpl.update");
		LOG.info("Updated by : {}", addressDTO);
		// log.info("{}",addressDAO.update(addressDTO));
		addressDTO.setUpdatedBy(userDAO.getUser(SecurityUtil.getUserId()));
		return addressDAO.update(addressDTO);
	}

}
