package com.ezeeinfo.service.impl;

import java.util.List;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ezeeinfo.dao.UserDAO;
import com.ezeeinfo.dto.UserDTO;
import com.ezeeinfo.exception.ServiceException;
import com.ezeeinfo.service.UserService;
import com.ezeeinfo.util.PasswordUtil;
import com.ezeeinfo.util.SecurityUtil;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private CacheManager cacheManager;

	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	@SuppressWarnings("unchecked")
	public List<UserDTO> getAllUsers(String namespaceCode) {

		Cache<String, List> cache = cacheManager.getCache("userListCache", String.class, List.class);
		List<UserDTO> users = (List<UserDTO>) cache.get(namespaceCode);
		if (users != null) {
			LOG.info("getAllUsers retrieved from cache");
		}
		if (users == null) {
			LOG.info("Hitting DB to getAllUsers");
			users = userDAO.getAllUsers(namespaceCode);
			if (users != null) {
				cache.put(namespaceCode, users);
			}
		}

		return users;
	}

	@Override
	public UserDTO getUserByCode(String code) {

		Cache<String, UserDTO> cache = cacheManager.getCache("userCache", String.class, UserDTO.class);
		UserDTO user = cache.get(code);
		if (user != null) {
			LOG.info("getUserByCode retrieved from cache");
		}
		if (user == null) {
			LOG.info("Hitting DB to getUserByCode");
			user = userDAO.getUserByCode(code);
			if (user != null) {
				cache.put(code, user);
			}
		}

		return user;
	}

	@Override
	public UserDTO update(UserDTO userDTO) {
		userDTO.setPassword(PasswordUtil.hashPassword(userDTO.getPassword()));
		userDTO.setUpdatedBy(userDAO.getUser(SecurityUtil.getUserId()));
		if (!userDTO.getNamespace().getCode().equals(userDAO.getUser(SecurityUtil.getUserId()).getNamespace().getCode())) {
			throw new ServiceException("Invalid Namespace. Enter valid Namespace");
		}
		UserDTO updatedUser = userDAO.update(userDTO);
		if (updatedUser != null) {
			Cache<String, UserDTO> userCache = cacheManager.getCache("userCache", String.class, UserDTO.class);
			userCache.put(updatedUser.getCode(), updatedUser);
			Cache<String, List> userListCache = cacheManager.getCache("userListCache", String.class, List.class);
			userListCache.remove(updatedUser.getNamespace().getCode());
			LOG.info("User cache updated and user list cache cleared");
		}

		return updatedUser;
	}
}