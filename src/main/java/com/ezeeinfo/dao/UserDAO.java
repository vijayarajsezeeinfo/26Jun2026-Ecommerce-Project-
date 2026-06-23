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
import com.ezeeinfo.dto.UserDTO;
import com.ezeeinfo.dto.enumeration.UserRoleEM;
import com.ezeeinfo.exception.ServiceException;
import com.ezeeinfo.util.PasswordUtil;

@Repository
public class UserDAO {
	@Autowired
	NamespaceDAO namespaceDAO;
	@Autowired
	UserDAO userDAO;

	private static final Logger LOG = LoggerFactory.getLogger(UserDAO.class);

	public List<UserDTO> getAllUsers(String namespaceCode) {
		String sql = "SELECT u.id AS user_id, u.code AS user_code, u.username AS user_username, u.password AS user_password, u.email AS user_email, u.mobile AS user_mobile, u.role AS user_role, u.namespace_id AS user_namespace_id, u.active_flag AS user_active_flag, u.updated_by AS user_updated_by FROM user u LEFT JOIN namespace n ON u.namespace_id = n.id WHERE u.active_flag < 2 AND n.code = ? ORDER BY u.id";
		List<UserDTO> userDTOs = new ArrayList<UserDTO>();
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, namespaceCode);
			try (ResultSet rs = statement.executeQuery();) {
				while (rs.next()) {

					NamespaceDTO namespaceDTO = namespaceDAO.getNamespaceByCode(namespaceCode);
					UserDTO updatedBy = userDAO.getUser(rs.getInt("user_updated_by"));

					UserDTO userDTO = new UserDTO();
					userDTO.setId(rs.getInt("user_id"));
					userDTO.setCode(rs.getString("user_code"));
					userDTO.setUsername(rs.getString("user_username"));
					userDTO.setPassword(rs.getString("user_password"));
					userDTO.setEmail(rs.getString("user_email"));
					userDTO.setMobile(rs.getString("user_mobile"));
					userDTO.setRole(UserRoleEM.getUserRoleEM(rs.getInt("user_role")));
					userDTO.setNamespace(namespaceDTO);
					userDTO.setActiveFlag(rs.getInt("user_active_flag"));
					userDTO.setUpdatedBy(updatedBy);

					userDTOs.add(userDTO);
				}
			}
			catch (SQLException e) {
				LOG.info("SQL Exception while getAllUsers. {}", e);
			}

		}
		catch (SQLException e) {
			LOG.info("SQL Exception while getAllUsers. {}", e);
		}
		return userDTOs;
	}

	public UserDTO getUserByCode(String code) {
		String sql = "SELECT u.id AS user_id, u.code AS user_code, u.username AS user_username, u.password AS user_password, u.email AS user_email, u.mobile AS user_mobile, u.role AS user_role, u.namespace_id AS user_namespace_id, u.active_flag AS user_active_flag, u.updated_by AS user_updated_by, n.code AS namespace_code FROM user u LEFT JOIN namespace n ON u.namespace_id = n.id WHERE u.active_flag < 2 AND u.code=?";
		UserDTO userDTO = null;
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, code);
			try (ResultSet rs = statement.executeQuery();) {
				if (!rs.next()) {
					throw new ServiceException("EXCEPTION 404: User Not Found");
				}
				NamespaceDTO namespaceDTO = namespaceDAO.getNamespaceByCode(rs.getString("namespace_code"));
				UserDTO updatedBy = userDAO.getUser(rs.getInt("user_updated_by"));
				userDTO = new UserDTO();
				userDTO.setId(rs.getInt("user_id"));
				userDTO.setCode(rs.getString("user_code"));
				userDTO.setUsername(rs.getString("user_username"));
				userDTO.setPassword(rs.getString("user_password"));
				userDTO.setEmail(rs.getString("user_email"));
				userDTO.setMobile(rs.getString("user_mobile"));
				userDTO.setRole(UserRoleEM.getUserRoleEM(rs.getInt("user_role")));
				userDTO.setNamespace(namespaceDTO);
				userDTO.setActiveFlag(rs.getInt("user_active_flag"));
				userDTO.setUpdatedBy(updatedBy);

			}
		}
		catch (SQLException e) {
			LOG.info("SQLEXCEPTION while getUserByCode. {}", e);
		}
		return userDTO;
	}

	public UserDTO update(UserDTO userDTO) {
		String sql = "{CALL EZEE_SP_USER_IUD(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

		try (Connection connection = DBConfig.getInstance().getConnection(); CallableStatement statement = connection.prepareCall(sql);) {

			Integer id = namespaceDAO.getNamespaceId(userDTO.getNamespace().getCode());
			statement.setString(1, userDTO.getCode());
			statement.setString(2, userDTO.getUsername());
			statement.setString(3, userDTO.getPassword());
			statement.setString(4, userDTO.getEmail());
			statement.setString(5, userDTO.getMobile());
			statement.setInt(6, userDTO.getRole().getId());
			statement.setInt(7, id);
			statement.setInt(8, userDTO.getActiveFlag());
			statement.setInt(9, userDTO.getUpdatedBy().getId());
			statement.setInt(10, 0);

			statement.registerOutParameter(1, Types.VARCHAR);
			statement.registerOutParameter(11, Types.INTEGER);

			statement.execute();
			LOG.info("EZEE_SP_USER_IUD Successfully executed");
			userDTO.setCode(statement.getString(1));

		}
		catch (SQLException e) {
			LOG.info("SQLEXCEPTION while update User using EZEE_SP_USER_IUD. {}", e);
		}
		return userDTO;

	}

	public UserDTO getUser(Integer id) {
		String sql = "SELECT usr.id AS user_id, usr.code AS user_code, usr.username AS user_username, usr.namespace_id AS user_namespace_id, usr.password AS user_password, usr.email AS user_email, usr.mobile AS user_mobile, usr.role AS user_role, usr.active_flag AS user_active_flag, usr.updated_by AS user_updated_by, ub.id AS updated_user_id, ub.code AS updated_user_code, ub.username AS updated_user_username, ub.namespace_id AS updated_user_namespace_id, ub.password AS updated_user_password, ub.email AS updated_user_email, ub.mobile AS updated_user_mobile, ub.role AS updated_user_role, ub.active_flag AS updated_user_active_flag, ub.updated_by AS updated_user_updated_by FROM `user` usr LEFT JOIN `user` ub ON usr.updated_by = ub.id WHERE usr.id = ?";
		UserDTO userDTO = null;
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setInt(1, id);
			try (ResultSet rs = statement.executeQuery();) {
				if (!rs.next()) {
					throw new ServiceException("EXCEPTION 404: User Not Found");
				}

				NamespaceDTO namespaceDTO = namespaceDAO.getNamespace(rs.getInt("updated_user_namespace_id"));
				UserDTO updatedBy = new UserDTO();
				updatedBy.setId(rs.getInt("updated_user_id"));
				updatedBy.setCode(rs.getString("updated_user_code"));
				updatedBy.setUsername(rs.getString("updated_user_username"));
				updatedBy.setNamespace(namespaceDTO);
				updatedBy.setPassword(rs.getString("updated_user_password"));
				updatedBy.setEmail(rs.getString("updated_user_email"));
				updatedBy.setMobile(rs.getString("updated_user_mobile"));
				updatedBy.setRole(UserRoleEM.getUserRoleEM(rs.getInt("updated_user_role")));
				updatedBy.setActiveFlag(rs.getInt("updated_user_active_flag"));

				NamespaceDTO namespaceDTO2 = namespaceDAO.getNamespace(rs.getInt("user_namespace_id"));
				userDTO = new UserDTO();
				userDTO.setId(rs.getInt("user_id"));
				userDTO.setCode(rs.getString("user_code"));
				userDTO.setUsername(rs.getString("user_username"));
				userDTO.setPassword(rs.getString("user_password"));
				userDTO.setEmail(rs.getString("user_email"));
				userDTO.setMobile(rs.getString("user_mobile"));
				userDTO.setRole(UserRoleEM.getUserRoleEM(rs.getInt("user_role")));
				userDTO.setNamespace(namespaceDTO2);
				userDTO.setActiveFlag(rs.getInt("user_active_flag"));
				userDTO.setUpdatedBy(updatedBy);

			}
			catch (SQLException e) {
				LOG.info("SQLException while getting User. {}", e);
			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while getting User. {}", e);
		}
		return userDTO;
	}

	public UserDTO login(String username, String password, String namespaceCode) {

		String sql = "SELECT u.id AS user_id , u.code AS user_code, u.username AS user_username, u.namespace_id AS user_namespace_id, u.password AS user_password, u.email AS user_email, mobile AS user_mobile, u.role AS user_role, u.active_flag AS user_active_flag, u.updated_by AS user_updated_by, n.code AS namespace_code FROM `user` u LEFT JOIN namespace n ON u.namespace_id=n.id WHERE u.username =? AND n.code=?";
		UserDTO userDTO = null;
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, username);
			statement.setString(2, namespaceCode);
			try (ResultSet rs = statement.executeQuery();) {
				if (!rs.next()) {
					throw new ServiceException("User " + username + " Not Found");
				}
				String dbPassword = rs.getString("user_password");
				boolean match = PasswordUtil.ENCODER.matches(password, dbPassword);
				if (!match) {
					throw new ServiceException("Invalid Password");
				}
				NamespaceDTO namespaceDTO = namespaceDAO.getNamespaceByCode(namespaceCode);
				UserDTO updatedBy = getUser(rs.getInt("user_updated_by"));
				userDTO = new UserDTO();
				userDTO.setId(rs.getInt("user_id"));
				userDTO.setCode(rs.getString("user_code"));
				userDTO.setUsername(rs.getString("user_username"));
				userDTO.setPassword(rs.getString("user_password"));
				userDTO.setEmail(rs.getString("user_email"));
				userDTO.setMobile(rs.getString("user_mobile"));
				userDTO.setRole(UserRoleEM.getUserRoleEM(rs.getInt("user_role")));
				userDTO.setNamespace(namespaceDTO);
				userDTO.setActiveFlag(rs.getInt("user_active_flag"));
				userDTO.setUpdatedBy(updatedBy);

			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while login");
		}
		return userDTO;
	}
}
