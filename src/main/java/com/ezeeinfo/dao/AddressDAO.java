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
import com.ezeeinfo.dto.AddressDTO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.dto.UserDTO;
import com.ezeeinfo.exception.ServiceException;

@Repository

public class AddressDAO {
	@Autowired
	UserDAO userDAO;
	@Autowired
	NamespaceDAO namespaceDAO;

	private static final Logger LOG = LoggerFactory.getLogger(AddressDAO.class);

	public List<AddressDTO> getAllAddresses(String namespaceCode) {

		String sql = "SELECT a.id AS address_id, a.code AS address_code, a.door_no AS address_door_no, a.street AS address_street, a.place AS address_place, a.city AS address_city, a.state AS address_state, a.country AS address_country, a.pincode AS address_pincode, a.user_id AS address_user_id, a.namespace_id AS address_namespace_id, a.active_flag AS address_active_flag, a.updated_by AS address_updated_by FROM address a LEFT JOIN namespace n ON a.namespace_id=n.id WHERE a.active_flag < 2 AND n.code=? ";
		List<AddressDTO> addressDTOs = new ArrayList<AddressDTO>();
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, namespaceCode);
			LOG.info("DataSource Class: {}", DBConfig.getInstance().getClass().getName());
			try (ResultSet rs = statement.executeQuery();) {
				while (rs.next()) {
					UserDTO userDTO = userDAO.getUser(rs.getInt("address_user_id"));
					NamespaceDTO namespaceDTO = namespaceDAO.getNamespace(rs.getInt("address_namespace_id"));
					UserDTO updatedBy = userDAO.getUser(rs.getInt("address_updated_by"));
					
					AddressDTO addressDTO = new AddressDTO();
					addressDTO.setId(rs.getInt("address_id"));
					addressDTO.setCode(rs.getString("address_code"));
					addressDTO.setDoorNo(rs.getString("address_door_no"));
					addressDTO.setStreet(rs.getString("address_street"));
					addressDTO.setPlace(rs.getString("address_place"));
					addressDTO.setCity(rs.getString("address_city"));
					addressDTO.setState(rs.getString("address_state"));
					addressDTO.setCountry(rs.getString("address_country"));
					addressDTO.setPincode(rs.getInt("address_pincode"));
					addressDTO.setUser(userDTO);
					addressDTO.setNamespace(namespaceDTO);
					addressDTO.setActiveFlag(rs.getInt("address_active_flag"));
					addressDTO.setUpdatedBy(updatedBy);
					addressDTOs.add(addressDTO);
				}
			}
			catch (SQLException e) {
				LOG.info("SQLException while getAllAddresses. {}", e);
			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while getAllAddresses. {}", e);
		}
		return addressDTOs;
	}

	public AddressDTO getAddressByCode(String code) {
		String sql = "SELECT a.id AS address_id, a.code AS address_code, a.door_no AS address_door_no, a.street AS address_street, a.place AS address_place, a.city AS address_city, a.state AS address_state, a.country AS address_country, a.pincode AS address_pincode, a.user_id AS address_user_id, a.namespace_id AS address_namespace_id, a.active_flag AS address_active_flag, a.updated_by AS address_updated_by FROM address a LEFT JOIN namespace n ON a.namespace_id=n.id WHERE a.active_flag < 2 AND a.code=? ";
		AddressDTO addressDTO = null;
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, code);
			LOG.info("DataSource Class: {}", DBConfig.getInstance().getClass().getName());
			try (ResultSet rs = statement.executeQuery();) {
				if (!rs.next()) {
					throw new ServiceException("EXCEPTION 404: Address Not Found");
				}
				UserDTO userDTO = userDAO.getUser(rs.getInt("address_user_id"));
				NamespaceDTO namespaceDTO = namespaceDAO.getNamespace(rs.getInt("address_namespace_id"));
				UserDTO updatedBy = userDAO.getUser(rs.getInt("address_updated_by"));

				addressDTO = new AddressDTO();
				addressDTO.setId(rs.getInt("address_id"));
				addressDTO.setCode(rs.getString("address_code"));
				addressDTO.setDoorNo(rs.getString("address_door_no"));
				addressDTO.setStreet(rs.getString("address_street"));
				addressDTO.setPlace(rs.getString("address_place"));
				addressDTO.setCity(rs.getString("address_city"));
				addressDTO.setState(rs.getString("address_state"));
				addressDTO.setCountry(rs.getString("address_country"));
				addressDTO.setPincode(rs.getInt("address_pincode"));
				addressDTO.setUser(userDTO);
				addressDTO.setNamespace(namespaceDTO);
				addressDTO.setActiveFlag(rs.getInt("address_active_flag"));
				addressDTO.setUpdatedBy(updatedBy);

			}
			catch (SQLException e) {
				LOG.info("SQLException while executing getAddressByCode. {}", e);
			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while executing getAddressByCode. {}", e);
		}
		return addressDTO;
	}

	public AddressDTO update(AddressDTO addressDTO) {
		LOG.info("entered AddressDAO.update");
		String sql = "{CALL EZEE_SP_ADDRESS_IUD(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

		try (Connection connection = DBConfig.getInstance().getConnection(); CallableStatement statement = connection.prepareCall(sql);) {
			LOG.info("DataSource Class: {}", DBConfig.getInstance().getClass().getName());
			statement.setString(1, addressDTO.getCode());
			statement.setString(2, addressDTO.getDoorNo());
			statement.setString(3, addressDTO.getStreet());
			statement.setString(4, addressDTO.getPlace());
			statement.setString(5, addressDTO.getCity());
			statement.setString(6, addressDTO.getState());
			statement.setString(7, addressDTO.getCountry());
			statement.setInt(8, addressDTO.getPincode());
			statement.setString(9, addressDTO.getUser().getCode());
			statement.setString(10, addressDTO.getNamespace().getCode());
			statement.setInt(11, addressDTO.getActiveFlag());
			statement.setInt(12, addressDTO.getUpdatedBy().getId());
			statement.setInt(13, 0);

			statement.registerOutParameter(1, Types.VARCHAR);
			statement.registerOutParameter(14, Types.INTEGER);

			statement.execute();
			LOG.info("EZEE_SP_ADDRESS_IUD successfully executed.");
			addressDTO.setCode(statement.getString(1));

		}
		catch (SQLException e) {
			LOG.info("SQLException while executing EZEE_SP_ADDRESS_IUD", e);
		}
		return addressDTO;
	}

}
