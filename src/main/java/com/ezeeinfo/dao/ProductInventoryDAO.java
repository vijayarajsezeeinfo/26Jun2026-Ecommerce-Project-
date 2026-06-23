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
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.dto.ProductDTO;
import com.ezeeinfo.dto.ProductInventoryDTO;
import com.ezeeinfo.dto.UserDTO;
import com.ezeeinfo.exception.ServiceException;

@Repository
public class ProductInventoryDAO {
	@Autowired
	NamespaceDAO namespaceDAO;
	@Autowired
	ProductDAO productDAO;
	@Autowired
	UserDAO userDAO;

	private static final Logger LOG = LoggerFactory.getLogger(ProductInventoryDAO.class);

	public List<ProductInventoryDTO> getAllProductInventories(String namespaceCode) {
		String sql = "SELECT pi.id AS product_inventory_id, pi.code AS product_inventory_code, p.code AS product_code, n.code AS namespace_code, p.name AS product_name, pi.product_id AS product_inventory_product_id, pi.available_quantity AS product_inventory_available_quantity, pi.namespace_id AS product_inventory_namespace_id, pi.active_flag AS product_inventory_active_flag, pi.updated_by AS product_inventory_updated_by FROM product_inventory `pi` LEFT JOIN products p ON pi.product_id = p.id LEFT JOIN namespace n ON pi.namespace_id = n.id WHERE pi.active_flag < 2 AND n.code = ?";
		List<ProductInventoryDTO> productInventoryDTOs = new ArrayList<ProductInventoryDTO>();
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, namespaceCode);
			try (ResultSet rs = statement.executeQuery();) {
				while (rs.next()) {
					ProductDTO productDTO = productDAO.getProductByCode(rs.getString("product_code"));
					NamespaceDTO namespaceDTO = namespaceDAO.getNamespaceByCode(rs.getString("namespace_code"));
					UserDTO updatedBy = userDAO.getUser(rs.getInt("product_inventory_updated_by"));

					ProductInventoryDTO productInventoryDTO = new ProductInventoryDTO();
					productInventoryDTO.setId(rs.getInt("product_inventory_id"));
					productInventoryDTO.setCode(rs.getString("product_inventory_code"));
					productInventoryDTO.setProduct(productDTO);
					productInventoryDTO.setAvailableQuantity(rs.getInt("product_inventory_available_quantity"));
					productInventoryDTO.setNamespace(namespaceDTO);
					productInventoryDTO.setActiveFlag(rs.getInt("product_inventory_active_flag"));
					productInventoryDTO.setUpdatedBy(updatedBy);
					productInventoryDTOs.add(productInventoryDTO);
				}
			}
			catch (SQLException e) {
				LOG.info("SQLException while getAllProductInventories. {}", e);
			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while getAllProductInventories. {}", e);
		}
		return productInventoryDTOs;
	}

	public ProductInventoryDTO getProductInventoryByCode(String code) {
		String sql = "SELECT pi.id AS product_inventory_id, pi.code AS product_inventory_code, p.code AS product_code, n.code AS namespace_code, p.name AS product_name, pi.product_id AS product_inventory_product_id, pi.available_quantity AS product_inventory_available_quantity, pi.namespace_id AS product_inventory_namespace_id, pi.active_flag AS product_inventory_active_flag, pi.updated_by AS product_inventory_updated_by FROM product_inventory `pi` LEFT JOIN products p ON pi.product_id = p.id LEFT JOIN namespace n ON pi.namespace_id = n.id WHERE pi.active_flag < 2 AND pi.code = ?";
		ProductInventoryDTO productInventoryDTO = null;
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, code);
			try (ResultSet rs = statement.executeQuery();) {
				if (!rs.next()) {
					throw new ServiceException("EXCEPTION 404: Product Inventory Not Found");
				}
				ProductDTO productDTO = productDAO.getProductByCode(rs.getString("product_code"));
				NamespaceDTO namespaceDTO = namespaceDAO.getNamespaceByCode(rs.getString("namespace_code"));
				UserDTO updatedBy = userDAO.getUser(rs.getInt("product_inventory_updated_by"));

				
				productInventoryDTO = new ProductInventoryDTO();
				productInventoryDTO.setId(rs.getInt("product_inventory_id"));
				productInventoryDTO.setCode(rs.getString("product_inventory_code"));
				productInventoryDTO.setProduct(productDTO);
				productInventoryDTO.setAvailableQuantity(rs.getInt("product_inventory_available_quantity"));
				productInventoryDTO.setNamespace(namespaceDTO);
				productInventoryDTO.setActiveFlag(rs.getInt("product_inventory_active_flag"));
				productInventoryDTO.setUpdatedBy(updatedBy);
			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while getProductInventoryByCode. {}", e);
		}
		return productInventoryDTO;
	}

	public ProductInventoryDTO update(ProductInventoryDTO productInventoryDTO) {
		String sql = "{CALL EZEE_SP_PRODUCT_INVENTORY_IUD( ?, ?, ?, ?, ?, ?, ?, ? )}";
		try (Connection connection = DBConfig.getInstance().getConnection(); CallableStatement statement = connection.prepareCall(sql);) {
			statement.setString(1, productInventoryDTO.getCode());
			statement.setString(2, productInventoryDTO.getProduct().getCode());
			statement.setInt(3, productInventoryDTO.getAvailableQuantity());
			statement.setString(4, productInventoryDTO.getNamespace().getCode());
			statement.setInt(5, productInventoryDTO.getActiveFlag());
			statement.setInt(6, productInventoryDTO.getUpdatedBy().getId());
			statement.setInt(7, 0);

			statement.registerOutParameter(1, Types.VARCHAR);
			statement.registerOutParameter(8, Types.INTEGER);

			statement.execute();
			LOG.info("EZEE_SP_PRODUCT_INVENTORY_IUD successfully executed.");

			productInventoryDTO.setCode(statement.getString(1));
		}
		catch (SQLException e) {
			LOG.info("SQLException while EZEE_SP_PRODUCT_INVENTORY_IUD. {}", e);
		}
		return productInventoryDTO;
	}

	public Integer getAvailableQuantityByProductId(int productId) {

		String sql = "SELECT available_quantity " + "FROM product_inventory " + "WHERE product_id = ? AND active_flag = 1";
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, productId);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("available_quantity");
				}
			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while getting inventory quantity.", e);
		}

		return null;
	}
}
