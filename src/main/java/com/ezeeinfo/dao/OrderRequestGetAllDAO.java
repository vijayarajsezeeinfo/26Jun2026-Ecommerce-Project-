//package com.ezeeinfo.dao;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.encrypt.RsaAlgorithm;
//
//import com.ezeeinfo.config.DBConfig;
//import com.ezeeinfo.dto.BrandDTO;
//import com.ezeeinfo.dto.CategoryDTO;
//import com.ezeeinfo.dto.NamespaceDTO;
//import com.ezeeinfo.dto.OrderDTO;
//import com.ezeeinfo.dto.OrderItemDTO;
//import com.ezeeinfo.dto.OrderRequestDTO;
//import com.ezeeinfo.dto.PaymentDTO;
//import com.ezeeinfo.dto.ProductDTO;
//import com.ezeeinfo.dto.UserDTO;
//import com.ezeeinfo.dto.enumeration.BillingStatusEM;
//import com.ezeeinfo.dto.enumeration.OrderStatusEM;
//import com.ezeeinfo.dto.enumeration.PaymentModeEM;
//import com.ezeeinfo.dto.enumeration.UserRoleEM;
//import com.ezeeinfo.exception.ServiceException;
//
//public class OrderRequestGetAllDAO {
//	@Autowired
//
//	public List<OrderRequestDTO> getAllOrderRequest(String namespaceCode) {
//		String sql = "SELECT  o.id AS order_id, o.code AS order_code, o.user_id AS ordered_user_id, o.order_status, o.total_amount AS order_total_amount, o.order_date AS ordered_date, o.active_flag AS order_active_flag,  oi.id AS order_item_id, oi.code AS order_item_code,  oi.quantity AS order_item_quantity, oi.price AS order_item_price, oi.active_flag AS order_item_active_flag,  p.id AS payment_id, p.code AS payment_code, p.payment_mode, p.total_amount_to_pay, p.paid_amount,  p.balance_amount, p.billing_status, p.transaction_id, p.remarks,  p.active_flag AS payment_active_flag, pr.id AS product_id, pr.code AS product_code,   pr.name AS product_name,  pr.description AS product_description, pr.price AS product_price,   pr.active_flag AS product_active_flag,  b.id AS brand_id, b.code AS brand_code, b.name AS brand_name, b.active_flag AS brand_active_flag,  c.id AS category_id, c.code AS category_code, c.name AS category_name,  c.active_flag AS category_active_flag,  u.id AS user_id, u.code AS user_code, u.username AS user_username, u.email AS user_email,  u.mobile AS user_mobile,  u.role AS user_role, u.active_flag AS user_active_flag,   n.id AS namespace_id, n.code AS namespace_code, n.name AS namespace_name,  n.active_flag AS namespace_active_flag, oub.id AS order_updator_id, oub.code AS order_updator_code, oub.username AS order_updator_username, oub.email AS order_updator_email, oub.mobile AS order_updator_mobile, oub.role AS order_updator_role, oub.active_flag AS order_updator_active_flag,  oiub.id AS order_item_updator_id, oiub.code AS order_item_updator_code, oiub.username AS order_item_updator_username, oiub.email AS order_item_updator_email, oiub.mobile AS order_item_updator_mobile, oiub.role AS order_item_updator_role, oiub.active_flag AS order_item_updator_active_flag, pub.id AS payment_updator_id, pub.code AS payment_updator_code, pub.username AS payment_updator_username, pub.email AS payment_updator_email, pub.mobile AS payment_updator_mobile, pub.role AS payment_updator_role, pub.active_flag AS payment_updator_active_flag,  prub.id AS product_updator_id, prub.code AS product_updator_code, prub.username AS product_updator_username, prub.email AS product_updator_email, prub.mobile AS product_updator_mobile, prub.role AS product_updator_role, prub.active_flag AS product_updator_active_flag,  bub.id AS brand_updator_id, bub.code AS brand_updator_code, bub.username AS brand_updator_username, bub.email AS brand_updator_email, bub.mobile AS brand_updator_mobile, bub.role AS brand_updator_role, bub.active_flag AS brand_updator_active_flag,    cub.id AS category_updator_id, cub.code AS category_updator_code, cub.username AS category_updator_username, cub.email AS category_updator_email, cub.mobile AS category_updator_mobile, cub.role AS category_updator_role, cub.active_flag AS category_updator_active_flag, uub.id AS user_updator_id, uub.code AS user_updator_code, uub.username AS user_updator_username, uub.email AS user_updator_email, uub.mobile AS user_updator_mobile, uub.role AS user_updator_role, uub.active_flag AS user_updator_active_flag,  nub.id AS namespace_updator_id, nub.code AS namespace_updator_code, nub.username AS namespace_updator_username, nub.email AS namespace_updator_email, nub.mobile AS namespace_updator_mobile, nub.role AS namespace_updator_role, nub.active_flag AS namespace_updator_active_flag,  FROM orders o  LEFT JOIN order_items oi ON o.id = oi.order_id   INNER JOIN payments p ON o.id = p.order_id  LEFT JOIN products pr ON oi.product_id = pr.id  LEFT JOIN brands b ON pr.brand_id = b.id  LEFT JOIN categories c ON pr.category_id = c.id  INNER JOIN `user` u ON o.user_id = u.id  INNER JOIN namespace n ON o.namespace_id = n.id  INNER JOIN `user` oub ON o.updated_by = oub.id INNER JOIN `user` oiub ON oi.updated_by = oiub.id INNER JOIN `user` pub ON p.updated_by = pub.id INNER JOIN `user` prub ON pr.updated_by = prub.id INNER JOIN `user` bub ON b.updated_by = bub.id INNER JOIN `user` cub ON c.updated_by = cub.id  INNER JOIN `user` uub ON u.updated_by = uub.id INNER JOIN `user` nub ON n.updated_by = nub.id  WHERE o.active_flag < 2 AND n.code = ? ORDER BY o.id, oi.id;";
//		Map<Integer, OrderRequestDTO> orderMap = new LinkedHashMap<>();
//
//		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
//
//			statement.setString(1, namespaceCode);
//			try (ResultSet rs = statement.executeQuery()) {
//				while (rs.next()) {
//					int orderId = rs.getInt("order_id");
//
//					OrderRequestDTO dto = orderMap.get(orderId);
//					// only creating ORDER REQUEST DTO if is not available.
//					// To avoid creating new ORDER REQUEST DTO for each items
//					// (if dto is available for that order id)
//					if (dto == null) {
//
//						// UserDTO nsUpdatedBy =
//						// userDAO.getUser(rs.getInt("namespace_updated_by"));
//
//						UserDTO nsUpdatedBy = new UserDTO();
//						nsUpdatedBy.setId(rs.getInt("namespace_updator_id"));
//						nsUpdatedBy.setCode(rs.getString("namespace_updator_code"));
//						nsUpdatedBy.setUsername(rs.getString("namespace_updator_username"));
//						nsUpdatedBy.setEmail(rs.getString("namespace_updator_email"));
//						nsUpdatedBy.setMobile(rs.getString("namespace_updator_mobile"));
//						nsUpdatedBy.setRole(UserRoleEM.getUserRoleEM(rs.getInt("namespace_updator_role")));
//						nsUpdatedBy.setActiveFlag(rs.getInt("namespace_updator_active_flag"));
//
//						NamespaceDTO namespaceDTO = new NamespaceDTO();
//						namespaceDTO.setId(rs.getInt("namespace_id"));
//						namespaceDTO.setCode(rs.getString("namespace_code"));
//						namespaceDTO.setName(rs.getString("namespace_name"));
//						namespaceDTO.setActiveFlag(rs.getInt("namespace_active_flag"));
//						namespaceDTO.setUpdatedBy(nsUpdatedBy);
//
//						UserDTO usrUpdatedBy = new UserDTO();
//						usrUpdatedBy.setId(rs.getInt("user_updator_id"));
//						usrUpdatedBy.setCode(rs.getString("user_updator_code"));
//						usrUpdatedBy.setUsername(rs.getString("user_updator_username"));
//						usrUpdatedBy.setEmail(rs.getString("user_updator_email"));
//						usrUpdatedBy.setMobile(rs.getString("user_updator_mobile"));
//						usrUpdatedBy.setRole(UserRoleEM.getUserRoleEM(rs.getInt("user_updator_role")));
//						usrUpdatedBy.setActiveFlag(rs.getInt("user_updator_active_flag"));
//
//						UserDTO userDTO = new UserDTO();
//						userDTO.setId(rs.getInt("user_id"));
//						userDTO.setCode(rs.getString("user_code"));
//						userDTO.setUsername(rs.getString("user_username"));
//						userDTO.setNamespace(namespaceDTO);
//						userDTO.setEmail(rs.getString("user_email"));
//						userDTO.setMobile(rs.getString("user_mobile"));
//						userDTO.setRole(UserRoleEM.getUserRoleEM(rs.getInt("user_role")));
//						userDTO.setActiveFlag(rs.getInt("user_active_flag"));
//						userDTO.setUpdatedBy(usrUpdatedBy);
//
//						UserDTO odrUpdatedBy = new UserDTO();
//						odrUpdatedBy.setId(rs.getInt("order_updator_id"));
//						odrUpdatedBy.setCode(rs.getString("order_updator_code"));
//						odrUpdatedBy.setUsername(rs.getString("order_updator_username"));
//						odrUpdatedBy.setEmail(rs.getString("order_updator_email"));
//						odrUpdatedBy.setMobile(rs.getString("order_updator_mobile"));
//						odrUpdatedBy.setRole(UserRoleEM.getUserRoleEM(rs.getInt("order_updator_role")));
//						odrUpdatedBy.setActiveFlag(rs.getInt("order_updator_active_flag"));
//
//						OrderDTO orderDTO = new OrderDTO();
//						orderDTO.setId(orderId);
//						orderDTO.setCode(rs.getString("order_code"));
//						orderDTO.setUser(userDTO);
//						orderDTO.setOrderStatus(OrderStatusEM.getOrderStatusEM(rs.getInt("order_status")));
//						orderDTO.setTotalAmount(rs.getDouble("order_total_amount"));
//						orderDTO.setOrderDate(rs.getTimestamp("ordered_date").toLocalDateTime());
//						orderDTO.setNamespace(namespaceDTO);
//						orderDTO.setActiveFlag(rs.getInt("order_active_flag"));
//						orderDTO.setUpdatedBy(odrUpdatedBy);
//
//						UserDTO pmtUpdatedBy = new UserDTO();
//						pmtUpdatedBy.setId(rs.getInt("payment_updator_id"));
//						pmtUpdatedBy.setCode(rs.getString("payment_updator_code"));
//						pmtUpdatedBy.setUsername(rs.getString("payment_updator_username"));
//						pmtUpdatedBy.setEmail(rs.getString("payment_updator_email"));
//						pmtUpdatedBy.setMobile(rs.getString("payment_updator_mobile"));
//						pmtUpdatedBy.setRole(UserRoleEM.getUserRoleEM(rs.getInt("payment_updator_role")));
//						pmtUpdatedBy.setActiveFlag(rs.getInt("payment_updator_active_flag"));
//
//						PaymentDTO paymentDTO = new PaymentDTO();
//						paymentDTO.setId(rs.getInt("payment_id"));
//						paymentDTO.setCode(rs.getString("payment_code"));
//						paymentDTO.setOrder(orderDTO);
//						paymentDTO.setPaymentMode(PaymentModeEM.getPaymentModeEM(rs.getInt("payment_mode")));
//						paymentDTO.setTotalAmountToPay(rs.getDouble("total_amount_to_pay"));
//						paymentDTO.setPaidAmount(rs.getDouble("paid_amount"));
//						paymentDTO.setBalanceAmount(rs.getDouble("balance_amount"));
//						paymentDTO.setBillingStatus(BillingStatusEM.getBillingStatusEM(rs.getInt("billing_status")));
//						paymentDTO.setTransactionId(rs.getString("transaction_id"));
//						paymentDTO.setRemarks(rs.getString("remarks"));
//						paymentDTO.setNamespace(namespaceDTO);
//						paymentDTO.setActiveFlag(rs.getInt("payment_active_flag"));
//						paymentDTO.setUpdatedBy(pmtUpdatedBy);
//
//						dto = new OrderRequestDTO();
//						dto.setOrder(orderDTO);
//						dto.setPayment(paymentDTO);
//						dto.setOrderItems(new ArrayList<>());
//						orderMap.put(orderId, dto);
//					}
//
//					// Only adding item if it exists in table
//					if (rs.getObject("order_item_id") != null) {
//
//						UserDTO brdUpdatedBy = new UserDTO();
//						brdUpdatedBy.setId(rs.getInt("brand_updator_id"));
//						brdUpdatedBy.setCode(rs.getString("brand_updator_code"));
//						brdUpdatedBy.setUsername(rs.getString("brand_updator_username"));
//						brdUpdatedBy.setEmail(rs.getString("brand_updator_email"));
//						brdUpdatedBy.setMobile(rs.getString("brand_updator_mobile"));
//						brdUpdatedBy.setRole(UserRoleEM.getUserRoleEM(rs.getInt("brand_updator_role")));
//						brdUpdatedBy.setActiveFlag(rs.getInt("brand_updator_active_flag"));
//
//						BrandDTO brandDTO = new BrandDTO();
//						brandDTO.setId(rs.getInt("brand_id"));
//						brandDTO.setCode(rs.getString("brand_code"));
//						brandDTO.setName(rs.getString("brand_name"));
//						brandDTO.setNamespace(dto.getOrder().getNamespace());
//						brandDTO.setActiveFlag(rs.getInt("brand_active_flag"));
//						brandDTO.setUpdatedBy(brdUpdatedBy);
//
//						UserDTO ctgUpdatedBy = new UserDTO();
//						ctgUpdatedBy.setId(rs.getInt("category_updator_id"));
//						ctgUpdatedBy.setCode(rs.getString("category_updator_code"));
//						ctgUpdatedBy.setUsername(rs.getString("category_updator_username"));
//						ctgUpdatedBy.setEmail(rs.getString("category_updator_email"));
//						ctgUpdatedBy.setMobile(rs.getString("category_updator_mobile"));
//						ctgUpdatedBy.setRole(UserRoleEM.getUserRoleEM(rs.getInt("category_updator_role")));
//						ctgUpdatedBy.setActiveFlag(rs.getInt("category_updator_active_flag"));
//
//						CategoryDTO categoryDTO = new CategoryDTO();
//						categoryDTO.setId(rs.getInt("category_id"));
//						categoryDTO.setCode(rs.getString("category_code"));
//						categoryDTO.setName(rs.getString("category_name"));
//						categoryDTO.setNamespace(dto.getOrder().getNamespace());
//						categoryDTO.setActiveFlag(rs.getInt("category_active_flag"));
//						categoryDTO.setUpdatedBy(ctgUpdatedBy);
//
//						// UserDTO pdtUpdatedBy =
//						// userDAO.getUser(rs.getInt("product_updated_by"));
//						UserDTO pdtUpdatedBy = new UserDTO();
//						pdtUpdatedBy.setId(rs.getInt("product_updator_id"));
//						pdtUpdatedBy.setCode(rs.getString("product_updator_code"));
//						pdtUpdatedBy.setUsername(rs.getString("product_updator_username"));
//						pdtUpdatedBy.setEmail(rs.getString("product_updator_email"));
//						pdtUpdatedBy.setMobile(rs.getString("product_updator_mobile"));
//						pdtUpdatedBy.setRole(UserRoleEM.getUserRoleEM(rs.getInt("product_updator_role")));
//						pdtUpdatedBy.setActiveFlag(rs.getInt("product_updator_active_flag"));
//
//						ProductDTO productDTO = new ProductDTO();
//						productDTO.setId(rs.getInt("product_id"));
//						productDTO.setCode(rs.getString("product_code"));
//						productDTO.setName(rs.getString("product_name"));
//						productDTO.setDescription(rs.getString("product_description"));
//						productDTO.setPrice(rs.getDouble("product_price"));
//						productDTO.setBrand(brandDTO);
//						productDTO.setCategory(categoryDTO);
//						productDTO.setNamespace(dto.getOrder().getNamespace());
//						productDTO.setActiveFlag(rs.getInt("product_active_flag"));
//						productDTO.setUpdatedBy(pdtUpdatedBy);
//
//						UserDTO otmUpdatedBy = new UserDTO();
//						otmUpdatedBy.setId(rs.getInt("order_item_updator_id"));
//						otmUpdatedBy.setCode(rs.getString("order_item_updator_code"));
//						otmUpdatedBy.setUsername(rs.getString("order_item_updator_username"));
//						otmUpdatedBy.setEmail(rs.getString("order_item_updator_email"));
//						otmUpdatedBy.setMobile(rs.getString("order_item_updator_mobile"));
//						otmUpdatedBy.setRole(UserRoleEM.getUserRoleEM(rs.getInt("order_item_updator_role")));
//						otmUpdatedBy.setActiveFlag(rs.getInt("order_item_updator_active_flag"));
//
//						OrderItemDTO orderItemDTO = new OrderItemDTO();
//						orderItemDTO.setId(rs.getInt("order_item_id"));
//						orderItemDTO.setCode(rs.getString("order_item_code"));
//						orderItemDTO.setOrder(dto.getOrder());
//						orderItemDTO.setProduct(productDTO);
//						orderItemDTO.setQuantity(rs.getInt("order_item_quantity"));
//						orderItemDTO.setPrice(rs.getDouble("order_item_price"));
//						orderItemDTO.setNamespace(dto.getOrder().getNamespace());
//						orderItemDTO.setActiveFlag(rs.getInt("order_item_active_flag"));
//						orderItemDTO.setUpdatedBy(otmUpdatedBy);
//
//						dto.getOrderItems().add(orderItemDTO);
//					}
//				}
//			}
//		}
//		catch (SQLException e) {
//			LOG.error("SQLException while getAllOrders for namespace: {}", namespaceCode, e);
//			throw new ServiceException("SQLException while getAllOrders");
//		}
//		List<OrderRequestDTO> allOrders = new ArrayList<OrderRequestDTO>(orderMap.values());
//		return allOrders;
//	}
//	
//}
