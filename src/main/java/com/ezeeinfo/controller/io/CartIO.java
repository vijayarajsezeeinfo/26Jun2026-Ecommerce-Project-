package com.ezeeinfo.controller.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CartIO extends BaseIO {

	private UserIOResponse user;
	private NamespaceIO namespace;
}
