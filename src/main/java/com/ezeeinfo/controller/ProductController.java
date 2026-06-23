package com.ezeeinfo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ezeeinfo.controller.io.BrandIO;
import com.ezeeinfo.controller.io.CategoryIO;
import com.ezeeinfo.controller.io.NamespaceIO;
import com.ezeeinfo.controller.io.ProductIO;
import com.ezeeinfo.dto.BrandDTO;
import com.ezeeinfo.dto.CategoryDTO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.dto.ProductDTO;
import com.ezeeinfo.service.ProductService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {

	@Autowired
	ProductService productService;
	@Autowired
	NamespaceController namespaceController;
	@Autowired
	CategoryController categoryController;
	@Autowired
	BrandController brandController;

	@RequestMapping(value = "/{namespaceCode}", method = RequestMethod.GET)
	public List<ProductIO> getAllProducts(@PathVariable("namespaceCode") String namespaceCode) {
		return productService.getAllProducts(namespaceCode).stream().map(dto -> productDTOToIO(dto)).toList();
	}

	@RequestMapping(value = "/code/{code}", method = RequestMethod.GET)
	public ProductIO getProductById(@PathVariable("code") String code) {
		return productDTOToIO(productService.getProductByCode(code));
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public ProductIO update(@RequestBody ProductIO productIO) {
		log.info("product io: {}", productIO);
		return productDTOToIO(productService.update(productIOToDTO(productIO)));
	}

	public ProductIO productDTOToIO(ProductDTO productDTO) {
		BrandIO brandIO = brandController.brandDTOToIO(productDTO.getBrand());
		CategoryIO categoryIO = categoryController.categoryDTOToIO(productDTO.getCategory());
		NamespaceIO namespaceIO = namespaceController.namespaceDTOToIO(productDTO.getNamespace());
		ProductIO productIO = new ProductIO();
		productIO.setCode(productDTO.getCode());
		productIO.setName(productDTO.getName());
		productIO.setDescription(productDTO.getDescription());
		productIO.setPrice(productDTO.getPrice());
		productIO.setBrand(brandIO);
		productIO.setCategory(categoryIO);
		productIO.setNamespace(namespaceIO);
		productIO.setActiveFlag(productDTO.getActiveFlag());
		return productIO;
	}

	public ProductDTO productIOToDTO(ProductIO productIO) {
		BrandDTO brandDTO = brandController.brandIOToDTO(productIO.getBrand());
		CategoryDTO categoryDTO = categoryController.categoryIOToDTO(productIO.getCategory());
		NamespaceDTO namespaceDTO = namespaceController.namespaceIOToDTO(productIO.getNamespace());
		ProductDTO productDTO = new ProductDTO();
		productDTO.setCode(productIO.getCode());
		productDTO.setName(productIO.getName());
		productDTO.setDescription(productIO.getDescription());
		productDTO.setPrice(productIO.getPrice());
		productDTO.setBrand(brandDTO);
		productDTO.setCategory(categoryDTO);
		productDTO.setNamespace(namespaceDTO);
		productDTO.setActiveFlag(productIO.getActiveFlag());
		return productDTO;
	}
}
