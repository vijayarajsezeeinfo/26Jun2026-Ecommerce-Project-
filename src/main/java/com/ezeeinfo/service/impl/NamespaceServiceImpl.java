package com.ezeeinfo.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ezeeinfo.dao.NamespaceDAO;
import com.ezeeinfo.dao.UserDAO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.service.NamespaceService;
import com.ezeeinfo.util.SecurityUtil;

@Service
public class NamespaceServiceImpl implements NamespaceService {

	@Autowired
	private NamespaceDAO namespaceDAO;
	@Autowired
	UserDAO userDAO;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private static Logger LOG = LoggerFactory.getLogger(NamespaceServiceImpl.class);

	@Override
	@SuppressWarnings("unchecked")
	public List<NamespaceDTO> getAllNamespaces() {

		List<NamespaceDTO> namespaces = (List<NamespaceDTO>) redisTemplate.opsForValue().get("ALL_NAMESPACES");
		if (namespaces != null) {
			LOG.info("getAllNamespaces retrieved from cache");
		}
		if (namespaces == null) {
			LOG.info("Hitting DB to getAllNamespaces");
			namespaces = namespaceDAO.getAllNamespaces();
			if (namespaces != null) {
				redisTemplate.opsForValue().set("ALL_NAMESPACES", namespaces);
			}
		}

		return namespaces;
	}

	@Override
	public NamespaceDTO getNamespaceByCode(String code) {

		NamespaceDTO namespace = (NamespaceDTO) redisTemplate.opsForValue().get(code);
		if (namespace != null) {
			LOG.info("getNamespaceByCode retrieved from cache");
		}
		if (namespace == null) {
			LOG.info("Hitting DB to getNamespaceByCode");
			namespace = namespaceDAO.getNamespaceByCode(code);
			if (namespace != null) {
				redisTemplate.opsForValue().set(code, namespace);
			}
		}

		return namespace;
	}

	@Override
	public NamespaceDTO update(NamespaceDTO namespaceDTO) {
		namespaceDTO.setUpdatedBy(userDAO.getUser(SecurityUtil.getUserId()));
		NamespaceDTO updatedNamespace = namespaceDAO.update(namespaceDTO);
		if (updatedNamespace != null) {
			redisTemplate.opsForValue().set(updatedNamespace.getCode(), updatedNamespace);
			redisTemplate.delete("ALL_NAMESPACES");
			LOG.info("Namespace cache updated and namespace list cache cleared");
		}

		return updatedNamespace;
	}
}