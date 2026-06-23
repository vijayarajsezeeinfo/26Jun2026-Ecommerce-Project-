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
import com.ezeeinfo.dto.CategoryDTO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.dto.UserDTO;
import com.ezeeinfo.exception.ServiceException;

@Repository
public class CategoryDAO {

	@Autowired
	NamespaceDAO namespaceDAO;
	@Autowired
	UserDAO userDAO;

	private static final Logger LOG = LoggerFactory.getLogger(CategoryDAO.class);

	public List<CategoryDTO> getAllCategories(String namespaceCode) {
		String sql = "SELECT c.id AS category_id, c.code AS category_code, c.name AS category_name, c.namespace_id AS category_namespace_id, c.active_flag AS category_active_flag, c.updated_by AS category_updated_by, n.code AS namespace_code FROM categories c LEFT JOIN namespace n ON c.namespace_id=n.id WHERE c.active_flag < 2 AND n.code = ?";
		List<CategoryDTO> categoryDTOs = new ArrayList<CategoryDTO>();

		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, namespaceCode);
			try (ResultSet rs = statement.executeQuery();) {
				while (rs.next()) {
					NamespaceDTO namespaceDTO = namespaceDAO.getNamespace(rs.getInt("category_namespace_id"));
					UserDTO updatedBy = userDAO.getUser(rs.getInt("category_updated_by"));

					CategoryDTO categoryDTO = new CategoryDTO();
					categoryDTO.setId(rs.getInt("category_id"));
					categoryDTO.setCode(rs.getString("category_code"));
					categoryDTO.setName(rs.getString("category_name"));
					categoryDTO.setNamespace(namespaceDTO);
					categoryDTO.setActiveFlag(rs.getInt("category_active_flag"));
					categoryDTO.setUpdatedBy(updatedBy);
					categoryDTOs.add(categoryDTO);
				}
			}
			catch (SQLException e) {
				LOG.info("SQLException while getAllCategories. {}", e);
			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while getAllCategories. {}", e);
		}
		return categoryDTOs;
	}

	public CategoryDTO getCategoryByCode(String code) {
		String sql = "SELECT c.id AS category_id, c.code AS category_code, c.name AS category_name, c.namespace_id AS category_namespace_id, c.active_flag AS category_active_flag, c.updated_by AS category_updated_by, n.code AS namespace_code FROM categories c LEFT JOIN namespace n ON c.namespace_id=n.id WHERE c.active_flag < 2 AND c.code = ?";
		CategoryDTO categoryDTO = null;
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, code);
			try (ResultSet rs = statement.executeQuery();) {
				if (!rs.next()) {
					throw new ServiceException("EXCEPTION 404: Category Not Found");
				}
				NamespaceDTO namespaceDTO = namespaceDAO.getNamespace(rs.getInt("category_namespace_id"));
				UserDTO updatedBy = userDAO.getUser(rs.getInt("category_updated_by"));

				categoryDTO = new CategoryDTO();
				categoryDTO.setId(rs.getInt("category_id"));
				categoryDTO.setCode(rs.getString("category_code"));
				categoryDTO.setName(rs.getString("category_name"));
				categoryDTO.setNamespace(namespaceDTO);
				categoryDTO.setActiveFlag(rs.getInt("category_active_flag"));
				categoryDTO.setUpdatedBy(updatedBy);
			}
			catch (SQLException e) {
				LOG.info("SQLException while getCategoryByCode. {}", e);
			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while getCategoryByCode. {}", e);
		}
		return categoryDTO;
	}

	public CategoryDTO update(CategoryDTO categoryDTO) {
		String sql = "{CALL EZEE_SP_CATEGORY_IUD( ?, ?, ?, ?, ?, ?, ?)}";
		try (Connection connection = DBConfig.getInstance().getConnection(); CallableStatement statement = connection.prepareCall(sql);) {
			statement.setString(1, categoryDTO.getCode());
			statement.setString(2, categoryDTO.getName());
			LOG.info("Namespace Code : {}", categoryDTO.getNamespace().getCode());
			statement.setString(3, categoryDTO.getNamespace().getCode());
			statement.setInt(4, categoryDTO.getActiveFlag());
			statement.setInt(5, categoryDTO.getUpdatedBy().getId());
			statement.setInt(6, 0);

			statement.registerOutParameter(1, Types.VARCHAR);
			statement.registerOutParameter(7, Types.INTEGER);
			statement.execute();
			LOG.info("EZEE_SP_CATEGORY_IUD executed successfully");
			categoryDTO.setCode(statement.getString(1));
		}
		catch (SQLException e) {
			LOG.info("SQLException while executing EZEE_SP_CATEGORY_IUD. {}", e);
		}
		return categoryDTO;
	}

	public CategoryDTO getCategoryById(Integer id) {
		String sql = "SELECT `id`, `code`, `name`, `namespace_id`, `active_flag`, `updated_by` FROM `categories` WHERE id = ? ";
		CategoryDTO categoryDTO = null;
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setInt(1, id);
			try (ResultSet rs = statement.executeQuery();) {
				if (!rs.next()) {
					throw new ServiceException("EXCEPTION 404: Category Not Found");
				}
				NamespaceDTO namespaceDTO = namespaceDAO.getNamespace(rs.getInt("namespace_id"));
				UserDTO updatedBy = userDAO.getUser(rs.getInt("updated_by"));

				categoryDTO = new CategoryDTO();
				categoryDTO.setId(rs.getInt("id"));
				categoryDTO.setCode(rs.getString("code"));
				categoryDTO.setName(rs.getString("name"));
				categoryDTO.setNamespace(namespaceDTO);
				categoryDTO.setActiveFlag(rs.getInt("active_flag"));
				categoryDTO.setUpdatedBy(updatedBy);

			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while getCategoryById. {}", e);
		}
		return categoryDTO;
	}
}
