package com.ezeeinfo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CartDTO extends BaseDTO {

	private UserDTO user;
	private NamespaceDTO namespace;
}
