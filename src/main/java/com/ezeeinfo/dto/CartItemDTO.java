package com.ezeeinfo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CartItemDTO extends BaseDTO {
	private CartDTO cart;
	private ProductDTO product;
	private Integer quantity;
	private NamespaceDTO namespace;
}
