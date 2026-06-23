package com.ezeeinfo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ezeeinfo.controller.io.CategoryIO;
import com.ezeeinfo.controller.io.NamespaceIO;
import com.ezeeinfo.dto.CategoryDTO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.service.CategoryService;

@RestController
@RequestMapping("/category")
public class CategoryController {

	@Autowired
	CategoryService categoryService;
	@Autowired
	NamespaceController namespaceController;
	


	@RequestMapping(value = "/{namespaceCode}", method = RequestMethod.GET)
	public List<CategoryIO> getAllCategories(@PathVariable("namespaceCode") String namespaceCode) {
		return categoryService.getAllCategories(namespaceCode).stream().map(dto -> categoryDTOToIO(dto)).toList();
	}

	@RequestMapping(value = "/code/{code}", method = RequestMethod.GET)
	public CategoryIO getCategoryByCode(@PathVariable("code") String code) {
		return categoryDTOToIO(categoryService.getCategoryByCode(code));
	}

	@RequestMapping(value="/update" ,method = RequestMethod.POST)
	public CategoryIO update(@RequestBody CategoryIO categoryIO) {
		return categoryDTOToIO(categoryService.update(categoryIOToDTO(categoryIO)));
	}

	public CategoryIO categoryDTOToIO(CategoryDTO categoryDTO) {
		NamespaceIO namespaceIO = namespaceController.namespaceDTOToIO(categoryDTO.getNamespace());
		CategoryIO categoryIO = new CategoryIO();
		categoryIO.setCode(categoryDTO.getCode());
		categoryIO.setName(categoryDTO.getName());
		categoryIO.setNamespace(namespaceIO);
		categoryIO.setActiveFlag(categoryDTO.getActiveFlag());
		return categoryIO;
	}

	public CategoryDTO categoryIOToDTO(CategoryIO categoryIO) {
		NamespaceDTO namespaceDTO = namespaceController.namespaceIOToDTO(categoryIO.getNamespace());
		CategoryDTO categoryDTO = new CategoryDTO();
		categoryDTO.setCode(categoryIO.getCode());
		categoryDTO.setName(categoryIO.getName());
		categoryDTO.setNamespace(namespaceDTO);
		categoryDTO.setActiveFlag(categoryIO.getActiveFlag());
		return categoryDTO;
	}
}
