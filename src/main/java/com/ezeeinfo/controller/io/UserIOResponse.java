package com.ezeeinfo.controller.io;

import com.ezeeinfo.dto.enumeration.UserRoleEM;

import lombok.Data;

@Data
public class UserIOResponse {
	private String code;
	private String username;
	private NamespaceIO namespace;
	private String email;
	private String mobile;
	private UserRoleEM role;
	private Integer activeFlag;
}
