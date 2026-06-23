package com.ezeeinfo.controller.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CartItemIO extends BaseIO {

	private CartIO cart;
	private ProductIO product;
	private Integer quantity;
	private NamespaceIO namespace;
}
