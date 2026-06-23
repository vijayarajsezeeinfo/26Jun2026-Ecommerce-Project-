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
import com.ezeeinfo.dto.CartDTO;
import com.ezeeinfo.dto.CartItemDTO;
import com.ezeeinfo.dto.CategoryDTO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.dto.ProductDTO;
import com.ezeeinfo.dto.UserDTO;
import com.ezeeinfo.dto.enumeration.UserRoleEM;

@Repository
public class CartItemDAO {

	@Autowired
	UserDAO userDAO;

	@Autowired
	ProductDAO productDAO;

	@Autowired
	NamespaceDAO namespaceDAO;

	private static final Logger LOG = LoggerFactory.getLogger(CartItemDAO.class);

	public CartItemDTO update(CartItemDTO cartItemDTO) {

		String sql = "{CALL EZEE_SP_CART_ITEMS_IUD(?,?,?,?,?,?,?,?,?)}";

		try (Connection connection = DBConfig.getInstance().getConnection(); CallableStatement statement = connection.prepareCall(sql);) {

			statement.setString(1, cartItemDTO.getCode());
			statement.setString(2, cartItemDTO.getCart().getCode());
			statement.setString(3, cartItemDTO.getProduct().getCode());
			statement.setInt(4, cartItemDTO.getQuantity());
			statement.setString(5, cartItemDTO.getNamespace().getCode());
			statement.setInt(6, cartItemDTO.getActiveFlag());
			statement.setInt(7, cartItemDTO.getUpdatedBy().getId());
			statement.setInt(8, 0);

			statement.registerOutParameter(1, Types.VARCHAR);
			statement.registerOutParameter(9, Types.INTEGER);

			statement.execute();

			LOG.info("EZEE_SP_CART_ITEMS_IUD successfully executed.");

			cartItemDTO.setCode(statement.getString(1));

		}
		catch (SQLException e) {
			LOG.info("SQLException while executing EZEE_SP_CART_ITEMS_IUD. {}", e);
		}

		return cartItemDTO;
	}

	public List<CartItemDTO> getAllCartItems(String namespaceCode) {
		String sql = "SELECT ci.id AS cart_item_id, ci.code AS cart_item_code, ci.cart_id AS cart_item_cart_id, ci.product_id AS cart_item_product_id, ci.quantity AS cart_item_quantity, ci.namespace_id AS cart_item_namespace_id, ci.active_flag AS cart_item_active_flag, ci.updated_by AS cart_item_updated_by, c.id AS cart_id, c.code AS cart_code, c.user_id AS cart_user_id, c.namespace_id AS cart_namespace_id, c.active_flag AS cart_active_flag, c.updated_by AS cart_updated_by, n.id AS namespace_id, n.code AS namespace_code, n.name AS namespace_name, n.active_flag AS namespace_active_flag, n.updated_by AS namespace_updated_by, u.id AS user_id, u.code AS user_code, u.username AS user_username, u.namespace_id AS user_namespace_id, u.password AS user_password, u.email AS user_email, u.mobile AS user_mobile, u.role AS user_role, u.active_flag AS user_active_flag, u.updated_by AS user_updated_by, p.id AS product_id, p.code AS product_code, p.name AS product_name, p.description AS product_description, p.price AS product_price, p.brand_id AS product_brand_id, p.category_id AS product_category_id, p.namespace_id AS product_namespace_id, p.active_flag AS product_active_flag, p.updated_by AS product_updated_by, b.id AS brand_id, b.code AS brand_code, b.name AS brand_name, b.namespace_id AS brand_namespace_id, b.active_flag AS brand_active_flag, b.updated_by AS brand_updated_by, ct.id AS category_id, ct.code AS category_code, ct.name AS category_name, ct.namespace_id AS category_namespace_id, ct.active_flag AS category_active_flag, ct.updated_by AS category_updated_by FROM cart_items ci LEFT JOIN cart c ON ci.cart_id = c.id INNER JOIN products p ON ci.product_id = p.id INNER JOIN namespace n ON ci.namespace_id = n.id INNER JOIN `user` u ON c.user_id = u.id INNER JOIN brands b ON p.brand_id = b.id INNER JOIN categories ct ON p.category_id = ct.id WHERE ci.active_flag < 2 AND n.code = ?";
		List<CartItemDTO> cartItemDTOs = new ArrayList<CartItemDTO>();
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, namespaceCode);
			try (ResultSet rs = statement.executeQuery();) {

				while (rs.next()) {
					UserDTO nsUpdatedBy = userDAO.getUser(rs.getInt("namespace_updated_by"));

					NamespaceDTO namespaceDTO = new NamespaceDTO();
					namespaceDTO.setId(rs.getInt("namespace_id"));
					namespaceDTO.setCode(rs.getString("namespace_code"));
					namespaceDTO.setName(rs.getString("namespace_name"));
					namespaceDTO.setActiveFlag(rs.getInt("namespace_active_flag"));
					namespaceDTO.setUpdatedBy(nsUpdatedBy);

					// NamespaceDTO usrNamespace =
					// namespaceDAO.getNamespace(rs.getInt("user_namespace_id"));
					UserDTO usrUpdatedBy = userDAO.getUser(rs.getInt("user_updated_by"));
					UserDTO userDTO = new UserDTO();
					userDTO.setId(rs.getInt("user_id"));
					userDTO.setCode(rs.getString("user_code"));
					userDTO.setUsername(rs.getString("user_username"));
					userDTO.setNamespace(namespaceDTO);
					userDTO.setPassword(rs.getString("user_password"));
					userDTO.setEmail(rs.getString("user_email"));
					userDTO.setMobile(rs.getString("user_mobile"));
					userDTO.setRole(UserRoleEM.getUserRoleEM(rs.getInt("user_role")));
					userDTO.setActiveFlag(rs.getInt("user_active_flag"));
					userDTO.setUpdatedBy(usrUpdatedBy);

					// UserDTO ctUser =
					// userDAO.getUser(rs.getInt("cart_user_id"));
					// NamespaceDTO ctNamespace =
					// namespaceDAO.getNamespace(rs.getInt("cart_namespace_id"));
					UserDTO ctUpdatedBy = userDAO.getUser(rs.getInt("cart_updated_by"));
					CartDTO cartDTO = new CartDTO();
					cartDTO.setId(rs.getInt("cart_id"));
					cartDTO.setCode(rs.getString("cart_code"));
					cartDTO.setUser(userDTO);
					cartDTO.setNamespace(namespaceDTO);
					cartDTO.setActiveFlag(rs.getInt("cart_active_flag"));
					cartDTO.setUpdatedBy(ctUpdatedBy);

					UserDTO brdUpdatedBy = userDAO.getUser(rs.getInt("brand_updated_by"));
					BrandDTO brandDTO = new BrandDTO();
					brandDTO.setId(rs.getInt("brand_id"));
					brandDTO.setCode(rs.getString("brand_code"));
					brandDTO.setName(rs.getString("brand_name"));
					brandDTO.setActiveFlag(rs.getInt("brand_active_flag"));
					brandDTO.setNamespace(namespaceDTO);
					brandDTO.setUpdatedBy(brdUpdatedBy);

					UserDTO ctgUpdatedBy = userDAO.getUser(rs.getInt("category_updated_by"));
					CategoryDTO categoryDTO = new CategoryDTO();
					categoryDTO.setId(rs.getInt("category_id"));
					categoryDTO.setCode(rs.getString("category_code"));
					categoryDTO.setName(rs.getString("category_name"));
					categoryDTO.setActiveFlag(rs.getInt("category_active_flag"));
					categoryDTO.setNamespace(namespaceDTO);
					categoryDTO.setUpdatedBy(ctgUpdatedBy);

					UserDTO pdtUpdatedBy = userDAO.getUser(rs.getInt("product_updated_by"));
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
					productDTO.setUpdatedBy(pdtUpdatedBy);

					UserDTO ciUpdatedBy = userDAO.getUser(rs.getInt("cart_item_updated_by"));
					CartItemDTO cartItemDTO = new CartItemDTO();
					cartItemDTO.setId(rs.getInt("cart_item_id"));
					cartItemDTO.setCode(rs.getString("cart_item_code"));
					cartItemDTO.setCart(cartDTO);
					cartItemDTO.setProduct(productDTO);
					cartItemDTO.setQuantity(rs.getInt("cart_item_quantity"));
					cartItemDTO.setActiveFlag(rs.getInt("cart_item_active_flag"));
					cartItemDTO.setNamespace(namespaceDTO);
					cartItemDTO.setUpdatedBy(ciUpdatedBy);

					cartItemDTOs.add(cartItemDTO);
				}
			}
			catch (SQLException e) {
				LOG.info("SQLException while getAllCartItems. {}", e);
			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while getAllCartItems. {}", e);
		}
		return cartItemDTOs;
	}

}