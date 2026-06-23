package com.ezeeinfo.dto;

import lombok.Data;

@Data
public class BaseDTO {
	private Integer id;
	private String code;
	private String name;
	private Integer activeFlag;
	private UserDTO updatedBy;
}
