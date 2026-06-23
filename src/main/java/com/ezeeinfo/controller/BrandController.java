package com.ezeeinfo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ezeeinfo.controller.io.BrandIO;
import com.ezeeinfo.controller.io.NamespaceIO;
import com.ezeeinfo.dto.BrandDTO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.service.BrandService;

@RestController
@RequestMapping("/brand")
public class BrandController {

	@Autowired
	BrandService brandService;
	@Autowired
	NamespaceController namespaceController;

	@RequestMapping(value = "/{namespaceCode}", method = RequestMethod.GET)
	public List<BrandIO> getAllBrands(@PathVariable("namespaceCode") String namespaceCode) {
		return brandService.getAllBrands(namespaceCode).stream().map(dto -> brandDTOToIO(dto)).toList();
	}

	@RequestMapping(value = "/code/{code}", method = RequestMethod.GET)
	public BrandIO getBrandByCode(@PathVariable("code") String code) {
		return brandDTOToIO(brandService.getBrandByCode(code));
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public BrandIO update(@RequestBody BrandIO brandIO) {
		return brandDTOToIO(brandService.update(brandIOToDTO(brandIO)));
	}

	public BrandIO brandDTOToIO(BrandDTO brandDTO) {
		NamespaceIO namespaceIO = namespaceController.namespaceDTOToIO(brandDTO.getNamespace());
		BrandIO brandIO = new BrandIO();
		brandIO.setCode(brandDTO.getCode());
		brandIO.setName(brandDTO.getName());
		brandIO.setNamespace(namespaceIO);
		brandIO.setActiveFlag(brandDTO.getActiveFlag());
		return brandIO;
	}

	public BrandDTO brandIOToDTO(BrandIO brandIO) {
		NamespaceDTO namespaceDTO = namespaceController.namespaceIOToDTO(brandIO.getNamespace());
		BrandDTO brandDTO = new BrandDTO();
		brandDTO.setCode(brandIO.getCode());
		brandDTO.setName(brandIO.getName());
		brandDTO.setNamespace(namespaceDTO);
		brandDTO.setActiveFlag(brandIO.getActiveFlag());
		return brandDTO;
	}
}
