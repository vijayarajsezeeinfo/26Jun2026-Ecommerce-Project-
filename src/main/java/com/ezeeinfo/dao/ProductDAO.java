package com.ezeeinfo.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ezeeinfo.config.DBConfig;
import com.ezeeinfo.dto.BrandDTO;
import com.ezeeinfo.dto.CategoryDTO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.dto.ProductDTO;
import com.ezeeinfo.dto.UserDTO;
import com.ezeeinfo.exception.ServiceException;

@Repository
public class ProductDAO {

	@Autowired
	NamespaceDAO namespaceDAO;
	@Autowired
	BrandDAO brandDAO;
	@Autowired
	CategoryDAO categoryDAO;
	@Autowired
	UserDAO userDAO;

	private static final Logger LOG = LoggerFactory.getLogger(ProductDAO.class);

	public List<ProductDTO> getAllProducts(String namespaceCode) {
		String sql = "SELECT p.id AS product_id, p.code AS product_code, p.name AS product_name, n.code AS namespace_code, b.code AS brand_code, c.code AS category_code, p.description AS product_description, p.price AS product_price, p.brand_id AS product_brand_id, p.category_id AS product_category_id, p.namespace_id AS product_namespace_id, p.active_flag AS product_active_flag, p.updated_by AS product_updated_by FROM products p LEFT JOIN namespace n ON p.namespace_id = n.id LEFT JOIN brands b ON p.brand_id = b.id LEFT JOIN categories c ON p.category_id=c.id WHERE p.active_flag < 2 AND n.code = ? ";
		List<ProductDTO> productDTOs = new ArrayList<ProductDTO>();
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, namespaceCode);
			try (ResultSet rs = statement.executeQuery();) {
				while (rs.next()) {
					BrandDTO brandDTO = brandDAO.getBrandByCode(rs.getString("brand_code"));
					CategoryDTO categoryDTO = categoryDAO.getCategoryByCode(rs.getString("category_code"));
					NamespaceDTO namespaceDTO = namespaceDAO.getNamespaceByCode(rs.getString("namespace_code"));
					UserDTO updatedBy = userDAO.getUser(rs.getInt("product_updated_by"));

					ProductDTO productDTO = new ProductDTO();
					productDTO.setId(rs.getInt("product_id"));
					productDTO.setCode(rs.getString("product_code"));
					productDTO.setName(rs.getString("product_name"));
					productDTO.setDescription(rs.getString("product_description"));
					productDTO.setPrice(rs.getDouble("product_price"));
					productDTO.setBrand(brandDTO);
					productDTO.setCategory(categoryDTO);
					productDTO.setNamespace(namespaceDTO);
					productDTO.setActiveFlag(rs.getInt("product_active_flag"));
					productDTO.setUpdatedBy(updatedBy);
					productDTOs.add(productDTO);
				}
			}
			catch (SQLException e) {
				LOG.info("SQLException while getAllProducts. {}", e);
			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while getAllProducts. {}", e);
		}
		return productDTOs;
	}

	public ProductDTO getProductByCode(String code) {
		String sql = "SELECT p.id AS product_id, p.code AS product_code, p.name AS product_name, n.code AS namespace_code, b.code AS brand_code, c.code AS category_code, p.description AS product_description, p.price AS product_price, p.brand_id AS product_brand_id, p.category_id AS product_category_id, p.namespace_id AS product_namespace_id, p.active_flag AS product_active_flag, p.updated_by AS product_updated_by FROM products p LEFT JOIN namespace n ON p.namespace_id = n.id LEFT JOIN brands b ON p.brand_id = b.id LEFT JOIN categories c ON p.category_id=c.id WHERE p.active_flag < 2 AND p.code = ? ";
		ProductDTO productDTO = null;
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, code);
			try (ResultSet rs = statement.executeQuery();) {
				if (!rs.next()) {
					throw new ServiceException("EXCEPTION 404: Product Not Found");
				}
				BrandDTO brandDTO = brandDAO.getBrandByCode(rs.getString("brand_code"));
				CategoryDTO categoryDTO = categoryDAO.getCategoryByCode(rs.getString("category_code"));
				NamespaceDTO namespaceDTO = namespaceDAO.getNamespaceByCode(rs.getString("namespace_code"));
				UserDTO updatedBy = userDAO.getUser(rs.getInt("product_updated_by"));

				productDTO = new ProductDTO();
				productDTO.setId(rs.getInt("product_id"));
				productDTO.setCode(rs.getString("product_code"));
				productDTO.setName(rs.getString("product_name"));
				productDTO.setDescription(rs.getString("product_description"));
				productDTO.setPrice(rs.getDouble("product_price"));
				productDTO.setBrand(brandDTO);
				productDTO.setCategory(categoryDTO);
				productDTO.setNamespace(namespaceDTO);
				productDTO.setActiveFlag(rs.getInt("product_active_flag"));
				productDTO.setUpdatedBy(updatedBy);

			}
			catch (SQLException e) {
				LOG.info("SQLException while getProductByCode. {}", e);
			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while getProductByCode. {}", e);
		}
		return productDTO;
	}

	public ProductDTO update(ProductDTO productDTO) {
		LOG.info("product dto: {}", productDTO);
		String sql = "{CALL EZEE_SP_PRODUCT_IUD( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )}";
		try (Connection connection = DBConfig.getInstance().getConnection(); CallableStatement statement = connection.prepareCall(sql);) {
			statement.setString(1, productDTO.getCode());
			statement.setString(2, productDTO.getName());
			statement.setString(3, productDTO.getDescription());
			statement.setDouble(4, productDTO.getPrice());
			statement.setString(5, productDTO.getBrand().getCode());
			statement.setString(6, productDTO.getCategory().getCode());
			statement.setString(7, productDTO.getNamespace().getCode());
			statement.setInt(8, productDTO.getActiveFlag());
			statement.setInt(9, productDTO.getUpdatedBy().getId());
			statement.setInt(10, 0);

			statement.registerOutParameter(1, Types.VARCHAR);
			statement.registerOutParameter(11, Types.INTEGER);

			statement.execute();
			LOG.info("EZEE_SP_PRODUCT_IUD successfully executed.");

			productDTO.setCode(statement.getString(1));
		}
		catch (SQLException e) {
			LOG.info("SQLException while executing EZEE_SP_PRODUCT_IUD. {}", e);
		}
		return productDTO;
	}
}
