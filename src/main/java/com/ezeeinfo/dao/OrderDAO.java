package com.ezeeinfo.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ezeeinfo.config.DBConfig;
import com.ezeeinfo.controller.ProductController;
import com.ezeeinfo.dto.BrandDTO;
import com.ezeeinfo.dto.CategoryDTO;
import com.ezeeinfo.dto.NamespaceDTO;
import com.ezeeinfo.dto.OrderDTO;
import com.ezeeinfo.dto.OrderItemDTO;
import com.ezeeinfo.dto.OrderRequestDTO;
import com.ezeeinfo.dto.PaymentDTO;
import com.ezeeinfo.dto.ProductDTO;
import com.ezeeinfo.dto.UserDTO;
import com.ezeeinfo.dto.enumeration.BillingStatusEM;
import com.ezeeinfo.dto.enumeration.OrderStatusEM;
import com.ezeeinfo.dto.enumeration.PaymentModeEM;
import com.ezeeinfo.dto.enumeration.UserRoleEM;
import com.ezeeinfo.exception.ServiceException;

@Repository
public class OrderDAO {
	@Autowired
	ProductDAO productDAO;
	@Autowired
	ProductController productController;
	@Autowired
	UserDAO userDAO;
	@Autowired
	NamespaceDAO namespaceDAO;
	@Autowired
	ProductInventoryDAO productInventoryDAO;

	private static final Logger LOG = LoggerFactory.getLogger(OrderDAO.class);

	// when we insert in order table, same time it will reflect in order_items
	// and payments table
	// we can only update the order_status in orders table after that.

	public OrderRequestDTO update(OrderRequestDTO orderRequestDTO) {
		LOG.info("OrderRequest DTO : {}", orderRequestDTO);
		String namespaceCode = userDAO.getUserByCode(orderRequestDTO.getOrder().getUser().getCode()).getNamespace().getCode();
		NamespaceDTO namespaceDTO = namespaceDAO.getNamespaceByCode(namespaceCode);

		// if ordered product is exists or not================
		for (OrderItemDTO item : orderRequestDTO.getOrderItems()) {
			List<ProductDTO> availableProducts = productDAO.getAllProducts(namespaceCode);
			boolean isExists = availableProducts.stream().anyMatch(product -> product.getCode().equals(item.getProduct().getCode()));
			if (!isExists) {
				throw new ServiceException("Product Not Found");
			}

			ProductDTO productDTO = productDAO.getProductByCode(item.getProduct().getCode());

			if (productDTO == null) {
				throw new ServiceException("Product Not Found");
			}

			Integer currentQty = productInventoryDAO.getAvailableQuantityByProductId(productDTO.getId());

			if (currentQty == null) {
				throw new ServiceException("Inventory not found for Product : " + productDTO.getCode());
			}

			if (currentQty < item.getQuantity()) {
				throw new ServiceException("Less Stock. Available : " + currentQty);
			}
		}

		double actualAmount = 0.0;

		for (OrderItemDTO item : orderRequestDTO.getOrderItems()) {
			double price = productDAO.getProductByCode(item.getProduct().getCode()).getPrice();
			actualAmount += price * item.getQuantity();
		}

		orderRequestDTO.getOrder().setTotalAmount(actualAmount);
		orderRequestDTO.getPayment().setTotalAmountToPay(actualAmount);

		// paying amount check=========================================
		if (actualAmount > orderRequestDTO.getPayment().getPaidAmount()) {
			throw new ServiceException("Insufficient Amount. Your Order worths " + actualAmount);
		}

		// balance
		if (actualAmount < orderRequestDTO.getPayment().getPaidAmount()) {
			orderRequestDTO.getPayment().setBalanceAmount(orderRequestDTO.getPayment().getPaidAmount() - actualAmount);
		}

		// insert in orders table====================================
		String sql = "{CALL EZEE_SP_ORDER_IUD( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )}";

		String orderCode = null;
		try (Connection connection = DBConfig.getInstance().getConnection(); CallableStatement statement = connection.prepareCall(sql);) {

			statement.setString(1, orderRequestDTO.getOrder().getCode());
			statement.setString(2, orderRequestDTO.getOrder().getUser().getCode());
			statement.setInt(3, orderRequestDTO.getOrder().getOrderStatus().getId());
			statement.setDouble(4, orderRequestDTO.getOrder().getTotalAmount());
			statement.setTimestamp(5, Timestamp.valueOf(orderRequestDTO.getOrder().getOrderDate()));
			statement.setString(6, orderRequestDTO.getOrder().getNamespace().getCode());
			statement.setInt(7, orderRequestDTO.getOrder().getActiveFlag());
			statement.setInt(8, orderRequestDTO.getOrder().getUpdatedBy().getId());
			statement.setInt(9, 0);
			statement.registerOutParameter(1, Types.VARCHAR);
			statement.registerOutParameter(10, Types.INTEGER);
			statement.execute();
			orderCode = statement.getString(1);
		}
		catch (SQLException e) {
			LOG.info("SQLException while EZEE_SP_ORDER_IUD. {}", e);
		}

		String sql2 = "SELECT id, code, user_id, order_status, total_amount, order_date, namespace_id, active_flag, updated_by FROM orders WHERE code = ?";

		if (orderCode == null) {
			throw new ServiceException("Order code is not generated.");
		}

		OrderDTO orderDTO = null;

		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql2);) {

			statement.setString(1, orderCode);

			try (ResultSet rs = statement.executeQuery();) {
				if (!rs.next()) {
					throw new ServiceException("EXCEPTION 404: Order is not Found");
				}

				UserDTO userDTO = userDAO.getUser(rs.getInt("user_id"));
				UserDTO updatedBy = userDAO.getUser(rs.getInt("updated_by"));

				orderDTO = new OrderDTO();
				orderDTO.setId(rs.getInt("id"));
				orderDTO.setCode(rs.getString("code"));
				orderDTO.setUser(userDTO);
				orderDTO.setOrderStatus(OrderStatusEM.getOrderStatusEM(rs.getInt("order_status")));
				orderDTO.setTotalAmount(rs.getDouble("total_amount"));
				orderDTO.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
				orderDTO.setNamespace(namespaceDTO);
				orderDTO.setActiveFlag(rs.getInt("active_flag"));
				orderDTO.setUpdatedBy(updatedBy);

			}
			catch (SQLException e) {
				LOG.info("SQLException while getting order. {}", e);
			}

		}
		catch (SQLException e) {
			LOG.info("SQLException while getting order. {}", e);
		}

		orderRequestDTO.getPayment().setOrder(orderDTO);
		PaymentDTO paymentDTO = orderRequestDTO.getPayment();
		paymentDTO.setNamespace(namespaceDTO);
		paymentDTO.setActiveFlag(orderRequestDTO.getOrder().getActiveFlag());
		LOG.info("orderDTO.getCode() : ", orderDTO.getCode());

		// ONCE ORDERED, we cannot modify/delete PAYMENTS AND ORDER ITEMS TABLE.
		// ONLY INSERT ALLOWED IN PAYMENTS AND ORDER ITEMS TABLE

		if (orderDTO.getActiveFlag() == 1 && orderDTO.getCode() != null) {

			// insert in payments table
			String sql3 = "{CALL EZEE_SP_PAYMENT_IUD( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )}";
			try (Connection connection = DBConfig.getInstance().getConnection(); CallableStatement statement = connection.prepareCall(sql3);) {
				statement.setString(1, paymentDTO.getCode());
				statement.setInt(2, paymentDTO.getOrder().getId());
				statement.setInt(3, paymentDTO.getPaymentMode().getId());
				statement.setDouble(4, paymentDTO.getTotalAmountToPay());
				statement.setDouble(5, paymentDTO.getPaidAmount());
				statement.setDouble(6, paymentDTO.getBalanceAmount());
				statement.setInt(7, paymentDTO.getBillingStatus().getId());
				statement.setString(8, paymentDTO.getTransactionId());
				statement.setString(9, paymentDTO.getRemarks());
				statement.setInt(10, paymentDTO.getNamespace().getId());
				statement.setInt(11, paymentDTO.getActiveFlag());
				statement.setInt(12, paymentDTO.getUpdatedBy().getId());

				statement.registerOutParameter(1, Types.VARCHAR);
				statement.registerOutParameter(13, Types.INTEGER);

				statement.execute();
				LOG.info(" EZEE_SP_PAYMENT_IUD is executed.");
				paymentDTO.setCode(statement.getString(1));
			}
			catch (SQLException e) {
				LOG.info("SQLException while executing EZEE_SP_PAYMENT_IUD. {}", e);
			}

			// insert in order items table
			String sql4 = "{CALL EZEE_SP_ORDER_ITEMS_IUD( ?, ?, ?, ?, ?, ?, ?, ?, ? )}";
			try (Connection connection = DBConfig.getInstance().getConnection(); CallableStatement statement = connection.prepareCall(sql4);) {

				for (OrderItemDTO item : orderRequestDTO.getOrderItems()) {

					item.setOrder(orderDTO);

					ProductDTO productDTO = productDAO.getProductByCode(item.getProduct().getCode());

					item.setProduct(productDTO);

					item.setNamespace(namespaceDTO);

					item.setPrice(productDTO.getPrice());

					item.setActiveFlag(orderDTO.getActiveFlag());

					item.setUpdatedBy(orderDTO.getUpdatedBy());

					statement.setString(1, item.getCode());
					statement.setInt(2, item.getOrder().getId());
					statement.setInt(3, item.getProduct().getId());
					statement.setInt(4, item.getQuantity());
					statement.setDouble(5, item.getPrice());
					statement.setInt(6, item.getNamespace().getId());
					statement.setInt(7, item.getActiveFlag());
					statement.setInt(8, item.getUpdatedBy().getId());

					statement.registerOutParameter(1, Types.VARCHAR);
					statement.registerOutParameter(9, Types.INTEGER);

					statement.execute();
					LOG.info(" EZEE_SP_ORDER_ITEMS_IUD is executed.");

					item.setCode(statement.getString(1));

					LOG.info("Product Code from Request : {}", item.getProduct().getCode());
					LOG.info("Product Id from Request   : {}", item.getProduct().getId());

					Integer currentQty = productInventoryDAO.getAvailableQuantityByProductId(item.getProduct().getId());

					if (currentQty == null) {
						throw new ServiceException("Inventory not found for product : " + item.getProduct().getCode());
					}

					int remainingQty = currentQty - item.getQuantity();

					String sql5 = "UPDATE product_inventory " + "SET available_quantity = ? " + "WHERE product_id = ?";

					try (Connection connection2 = DBConfig.getInstance().getConnection(); PreparedStatement statement2 = connection2.prepareStatement(sql5)) {

						statement2.setInt(1, remainingQty);
						statement2.setInt(2, item.getProduct().getId());

						statement2.executeUpdate();
					}

				}

			}
			catch (SQLException e) {
				LOG.info("SQLException while executing EZEE_SP_ORDER_ITEMS_IUD. {}", e);
			}

		}

		orderRequestDTO.setOrder(orderDTO);
		orderRequestDTO.setPayment(paymentDTO);
		return orderRequestDTO;
	}

	public OrderRequestDTO getOrderByCode(String code) {
		String sql = "SELECT o.id AS order_id, o.code AS order_code, o.user_id AS ordered_user_id, o.order_status AS order_status, o.total_amount  AS order_total_amount, o.order_date AS ordered_date, o.namespace_id AS order_namespace_id, o.active_flag AS order_active_flag, o.updated_by AS order_updated_by, oi.id AS order_item_id, oi.code AS order_item_code,oi.order_id AS order_item_order_id, oi.product_id AS order_item_product_id, oi.quantity AS order_item_quantity, oi.price AS order_item_price, oi.namespace_id AS order_item_namespace_id, oi.active_flag AS order_item_namespace_id, oi.active_flag AS order_item_active_flag, oi.updated_by AS order_item_updated_by, p.id AS payment_id, p.code AS payment_code, p.order_id AS payment_order_id, p.payment_mode AS payment_mode,  p.total_amount_to_pay AS payment_total_amount_to_pay, p.paid_amount AS payment_paid_amount, p.balance_amount AS payment_balance_amount, p.billing_status AS payment_billing_status, p.transaction_id AS payment_transaction_id, p.remarks AS payment_remarks, p.namespace_id AS payment_namespace_id, p.active_flag AS payment_active_flag, p.updated_by AS payment_updated_by,  pr.id AS product_id, pr.code AS product_code, pr.name AS product_name, pr.description AS product_description, pr.price AS product_price, pr.brand_id AS product_brand_id, pr.category_id AS product_category_id, pr.namespace_id AS product_namespace_id, pr.active_flag AS product_active_flag, pr.updated_by AS product_updated_by, b.id AS brand_id, b.code AS brand_code, b.name AS brand_name, b.namespace_id AS brand_namespace_id, b.active_flag AS brand_active_flag, b.updated_by AS brand_updated_by,  c.id AS category_id, c.code AS category_code, c.name AS category_name, c.namespace_id AS category_namespace_id, c.active_flag AS category_active_flag, c.updated_by AS category_updated_by,  u.id AS user_id, u.code AS user_code, u.username AS user_username, u.namespace_id AS user_namespace_id, u.password AS user_password, u.email AS user_email, u.mobile AS user_mobile, u.role AS user_role, u.active_flag AS user_Active_flag, u.updated_by AS user_updated_by,  n.id AS namespace_id, n.code AS namespace_code, n.name AS namespace_name, n.active_flag AS namespace_active_flag, n.updated_by AS namespace_updated_by FROM orders o LEFT JOIN order_items oi ON o.id = oi.order_id INNER JOIN payments p ON o.id = p.order_id INNER JOIN products pr ON oi.product_id = pr.id INNER JOIN brands b ON pr.brand_id = b.id  INNER JOIN categories c ON pr.category_id = c.id  INNER JOIN `user` u ON o.user_id = u.id  INNER JOIN namespace n ON o.namespace_id = n.id  WHERE o.active_flag < 2 AND o.code = ?";
		OrderRequestDTO orderRequestDTO = null;
		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
			statement.setString(1, code);
			try (ResultSet rs = statement.executeQuery();) {

				NamespaceDTO namespaceDTO = null;
				UserDTO userDTO = null;
				OrderDTO orderDTO = null;
				PaymentDTO paymentDTO = null;

				orderRequestDTO = new OrderRequestDTO();
				List<OrderItemDTO> items = new ArrayList<OrderItemDTO>();
				orderRequestDTO.setOrderItems(items);

				while (rs.next()) {
					UserDTO updatedBy=userDAO.getUser(rs.getInt("namespace_updated_by"));
					namespaceDTO = new NamespaceDTO();
					namespaceDTO.setId(rs.getInt("namespace_id"));
					namespaceDTO.setCode(rs.getString("namespace_code"));
					namespaceDTO.setName(rs.getString("namespace_name"));
					namespaceDTO.setActiveFlag(rs.getInt("namespace_active_flag"));
					namespaceDTO.setUpdatedBy(updatedBy);

					UserDTO updatedBy2= userDAO.getUser(rs.getInt("user_updated_by"));
					userDTO = new UserDTO();
					userDTO.setId(rs.getInt("user_id"));
					userDTO.setCode(rs.getString("user_code"));
					userDTO.setUsername(rs.getString("user_username"));
					userDTO.setNamespace(namespaceDTO);
					userDTO.setPassword(rs.getString("user_password"));
					userDTO.setEmail(rs.getString("user_email"));
					userDTO.setMobile(rs.getString("user_mobile"));
					userDTO.setRole(UserRoleEM.getUserRoleEM(rs.getInt("user_role")));
					userDTO.setActiveFlag(rs.getInt("user_active_flag"));
					userDTO.setUpdatedBy(updatedBy2);

					UserDTO updatedBy3=userDAO.getUser(rs.getInt("order_updated_by"));
					orderDTO = new OrderDTO();
					orderDTO.setId(rs.getInt("order_id"));
					orderDTO.setCode(rs.getString("order_code"));
					orderDTO.setUser(userDTO);
					orderDTO.setOrderStatus(OrderStatusEM.getOrderStatusEM(rs.getInt("order_status")));
					orderDTO.setTotalAmount(rs.getDouble("order_total_amount"));
					orderDTO.setOrderDate(rs.getTimestamp("ordered_date").toLocalDateTime());
					orderDTO.setNamespace(namespaceDTO);
					orderDTO.setActiveFlag(rs.getInt("order_active_flag"));
					orderDTO.setUpdatedBy(updatedBy3);

					UserDTO updatedBy4 = userDAO.getUser(rs.getInt("payment_updated_by"));
					paymentDTO = new PaymentDTO();
					paymentDTO.setId(rs.getInt("payment_id"));
					paymentDTO.setCode(rs.getString("payment_code"));
					paymentDTO.setOrder(orderDTO);
					paymentDTO.setPaymentMode(PaymentModeEM.getPaymentModeEM(rs.getInt("payment_mode")));
					paymentDTO.setTotalAmountToPay(rs.getDouble("payment_total_amount_to_pay"));
					paymentDTO.setPaidAmount(rs.getDouble("payment_paid_amount"));
					paymentDTO.setBalanceAmount(rs.getDouble("payment_balance_amount"));
					paymentDTO.setBillingStatus(BillingStatusEM.getBillingStatusEM(rs.getInt("payment_billing_status")));
					paymentDTO.setTransactionId(rs.getString("payment_transaction_id"));
					paymentDTO.setRemarks(rs.getString("payment_remarks"));
					paymentDTO.setNamespace(namespaceDTO);
					paymentDTO.setActiveFlag(rs.getInt("payment_active_flag"));
					paymentDTO.setUpdatedBy(updatedBy4);

					UserDTO updatedBy5= userDAO.getUser(rs.getInt("brand_updated_by"));
					BrandDTO brandDTO = new BrandDTO();
					brandDTO.setId(rs.getInt("brand_id"));
					brandDTO.setCode(rs.getString("brand_code"));
					brandDTO.setName(rs.getString("brand_name"));
					brandDTO.setNamespace(namespaceDTO);
					brandDTO.setActiveFlag(rs.getInt("brand_active_flag"));
					brandDTO.setUpdatedBy(updatedBy5);

					UserDTO updatedBy6 = userDAO.getUser(rs.getInt("category_updated_by"));
					CategoryDTO categoryDTO = new CategoryDTO();
					categoryDTO.setId(rs.getInt("category_id"));
					categoryDTO.setCode(rs.getString("category_code"));
					categoryDTO.setName(rs.getString("category_name"));
					categoryDTO.setNamespace(namespaceDTO);
					categoryDTO.setActiveFlag(rs.getInt("category_active_flag"));
					categoryDTO.setUpdatedBy(updatedBy6);
					
					
                    UserDTO updatedBy7=userDAO.getUser(rs.getInt("product_updated_by"));
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
					productDTO.setUpdatedBy(updatedBy7);

					UserDTO updatedBy8= userDAO.getUser(rs.getInt("order_item_updated_by"));
					OrderItemDTO orderItemDTO = new OrderItemDTO();
					orderItemDTO.setId(rs.getInt("order_item_id"));
					orderItemDTO.setCode(rs.getString("order_item_code"));
					orderItemDTO.setOrder(orderDTO);
					orderItemDTO.setProduct(productDTO);
					orderItemDTO.setQuantity(rs.getInt("order_item_quantity"));
					orderItemDTO.setPrice(rs.getDouble("order_item_price"));
					orderItemDTO.setNamespace(namespaceDTO);
					orderItemDTO.setActiveFlag(rs.getInt("order_item_active_flag"));
					orderItemDTO.setUpdatedBy(updatedBy8);

					if (orderItemDTO != null) {
						items.add(orderItemDTO);
					}
				}

				if (orderDTO == null) {
					LOG.info("Order Not Found for code {}", code);
					throw new ServiceException("EXCEPTION 404: Order Not Found");
				}

				orderRequestDTO.setOrder(orderDTO);
				orderRequestDTO.setPayment(paymentDTO);
				orderRequestDTO.setOrderItems(items);
			}
			catch (SQLException e) {
				LOG.info("SQLException while getOrderByCode. {}", e);
			}
		}
		catch (SQLException e) {
			LOG.info("SQLException while getOrderByCode. {}", e);
		}
		LOG.info("Order is retrived from db for Order Code : {}", code);
		return orderRequestDTO;
	}

	public List<OrderRequestDTO> getAllOrders(String namespaceCode) {
		String sql = "SELECT o.id AS order_id, o.code AS order_code, o.user_id AS ordered_user_id, o.order_status, o.total_amount AS order_total_amount, o.order_date AS ordered_date,o.namespace_id AS order_namespace_id, o.active_flag AS order_active_flag, o.updated_by AS order_updated_by,oi.id AS order_item_id, oi.code AS order_item_code, oi.order_id AS order_item_order_id, oi.product_id AS order_item_product_id, oi.quantity AS order_item_quantity, oi.price AS order_item_price, oi.namespace_id AS order_item_namespace_id, oi.active_flag AS order_item_active_flag, oi.updated_by AS order_item_updated_by, p.id AS payment_id, p.code AS payment_code, p.order_id AS payment_order_id, p.payment_mode, p.total_amount_to_pay, p.paid_amount, p.balance_amount, p.billing_status, p.transaction_id, p.remarks, p.namespace_id AS payment_namespace_id, p.active_flag AS payment_active_flag, p.updated_by AS payment_updated_by,  pr.id AS product_id, pr.code AS product_code, pr.name AS product_name,  pr.description AS product_description, pr.price AS product_price,  pr.brand_id, pr.category_id, pr.namespace_id AS product_namespace_id,  pr.active_flag AS product_active_flag, pr.updated_by AS product_updated_by,  b.id AS brand_id, b.code AS brand_code, b.name AS brand_name, b.namespace_id AS brand_namespace_id, b.active_flag AS brand_active_flag, b.updated_by AS brand_updated_by, c.id AS category_id, c.code AS category_code, c.name AS category_name,  c.namespace_id AS category_namespace_id, c.active_flag AS category_active_flag, c.updated_by AS category_updated_by,  u.id AS user_id, u.code AS user_code, u.username AS user_username,  u.namespace_id AS user_namespace_id, u.email AS user_email, u.mobile AS user_mobile,  u.role AS user_role, u.active_flag AS user_active_flag, u.updated_by AS user_updated_by,  n.id AS namespace_id, n.code AS namespace_code, n.name AS namespace_name,  n.active_flag AS namespace_active_flag, n.updated_by AS namespace_updated_by FROM orders o LEFT JOIN order_items oi ON o.id = oi.order_id  INNER JOIN payments p ON o.id = p.order_id LEFT JOIN products pr ON oi.product_id = pr.id LEFT JOIN brands b ON pr.brand_id = b.id LEFT JOIN categories c ON pr.category_id = c.id INNER JOIN `user` u ON o.user_id = u.id INNER JOIN namespace n ON o.namespace_id = n.id WHERE o.active_flag < 2 AND n.code = ? ORDER BY o.id, oi.id";

		Map<Integer, OrderRequestDTO> orderMap = new LinkedHashMap<>();

		try (Connection connection = DBConfig.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, namespaceCode);
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					int orderId = rs.getInt("order_id");

					OrderRequestDTO dto = orderMap.get(orderId);
					if (dto == null) {
						
						UserDTO nsUpdatedBy=userDAO.getUser(rs.getInt("namespace_updated_by"));
						NamespaceDTO namespaceDTO = new NamespaceDTO();
						namespaceDTO.setId(rs.getInt("namespace_id"));
						namespaceDTO.setCode(rs.getString("namespace_code"));
						namespaceDTO.setName(rs.getString("namespace_name"));
						namespaceDTO.setActiveFlag(rs.getInt("namespace_active_flag"));
						namespaceDTO.setUpdatedBy(nsUpdatedBy);

						UserDTO usrUpdatedBy=userDAO.getUser(rs.getInt("user_updated_by"));
						UserDTO userDTO = new UserDTO();
						userDTO.setId(rs.getInt("user_id"));
						userDTO.setCode(rs.getString("user_code"));
						userDTO.setUsername(rs.getString("user_username"));
						userDTO.setNamespace(namespaceDTO);
						userDTO.setEmail(rs.getString("user_email"));
						userDTO.setMobile(rs.getString("user_mobile"));
						userDTO.setRole(UserRoleEM.getUserRoleEM(rs.getInt("user_role")));
						userDTO.setActiveFlag(rs.getInt("user_active_flag"));
						userDTO.setUpdatedBy(usrUpdatedBy);

						UserDTO odrUpdatedBy = userDAO.getUser(rs.getInt("order_updated_by"));
						OrderDTO orderDTO = new OrderDTO();
						orderDTO.setId(orderId);
						orderDTO.setCode(rs.getString("order_code"));
						orderDTO.setUser(userDTO);
						orderDTO.setOrderStatus(OrderStatusEM.getOrderStatusEM(rs.getInt("order_status")));
						orderDTO.setTotalAmount(rs.getDouble("order_total_amount"));
						orderDTO.setOrderDate(rs.getTimestamp("ordered_date").toLocalDateTime());
						orderDTO.setNamespace(namespaceDTO);
						orderDTO.setActiveFlag(rs.getInt("order_active_flag"));
						orderDTO.setUpdatedBy(odrUpdatedBy);

						UserDTO pmtUpdatedBy=userDAO.getUser(rs.getInt("payment_updated_by"));
						PaymentDTO paymentDTO = new PaymentDTO();
						paymentDTO.setId(rs.getInt("payment_id"));
						paymentDTO.setCode(rs.getString("payment_code"));
						paymentDTO.setOrder(orderDTO);
						paymentDTO.setPaymentMode(PaymentModeEM.getPaymentModeEM(rs.getInt("payment_mode")));
						paymentDTO.setTotalAmountToPay(rs.getDouble("total_amount_to_pay"));
						paymentDTO.setPaidAmount(rs.getDouble("paid_amount"));
						paymentDTO.setBalanceAmount(rs.getDouble("balance_amount"));
						paymentDTO.setBillingStatus(BillingStatusEM.getBillingStatusEM(rs.getInt("billing_status")));
						paymentDTO.setTransactionId(rs.getString("transaction_id"));
						paymentDTO.setRemarks(rs.getString("remarks"));
						paymentDTO.setNamespace(namespaceDTO);
						paymentDTO.setActiveFlag(rs.getInt("payment_active_flag"));
						paymentDTO.setUpdatedBy(pmtUpdatedBy);

						dto = new OrderRequestDTO();
						dto.setOrder(orderDTO);
						dto.setPayment(paymentDTO);
						dto.setOrderItems(new ArrayList<>());
						orderMap.put(orderId, dto);
					}

					// Only add item if it exists
					if (rs.getObject("order_item_id") != null) {
						
						UserDTO brdUpdatedBy = userDAO.getUser(rs.getInt("brand_updated_by"));
						BrandDTO brandDTO = new BrandDTO();
						brandDTO.setId(rs.getInt("brand_id"));
						brandDTO.setCode(rs.getString("brand_code"));
						brandDTO.setName(rs.getString("brand_name"));
						brandDTO.setNamespace(dto.getOrder().getNamespace());
						brandDTO.setActiveFlag(rs.getInt("brand_active_flag"));
						brandDTO.setUpdatedBy(brdUpdatedBy);

						UserDTO ctgUpdatedBy=userDAO.getUser(rs.getInt("category_updated_by"));
						CategoryDTO categoryDTO = new CategoryDTO();
						categoryDTO.setId(rs.getInt("category_id"));
						categoryDTO.setCode(rs.getString("category_code"));
						categoryDTO.setName(rs.getString("category_name"));
						categoryDTO.setNamespace(dto.getOrder().getNamespace());
						categoryDTO.setActiveFlag(rs.getInt("category_active_flag"));
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
						productDTO.setNamespace(dto.getOrder().getNamespace());
						productDTO.setActiveFlag(rs.getInt("product_active_flag"));
						productDTO.setUpdatedBy(pdtUpdatedBy);

						UserDTO  otmUpdatedBy= userDAO.getUser(rs.getInt("order_item_updated_by"));
						OrderItemDTO orderItemDTO = new OrderItemDTO();
						orderItemDTO.setId(rs.getInt("order_item_id"));
						orderItemDTO.setCode(rs.getString("order_item_code"));
						orderItemDTO.setOrder(dto.getOrder());
						orderItemDTO.setProduct(productDTO);
						orderItemDTO.setQuantity(rs.getInt("order_item_quantity"));
						orderItemDTO.setPrice(rs.getDouble("order_item_price"));
						orderItemDTO.setNamespace(dto.getOrder().getNamespace());
						orderItemDTO.setActiveFlag(rs.getInt("order_item_active_flag"));
						orderItemDTO.setUpdatedBy(otmUpdatedBy);

						dto.getOrderItems().add(orderItemDTO);
					}
				}
			}
		}
		catch (SQLException e) {
			LOG.error("SQLException while getAllOrders for namespace: {}", namespaceCode, e);
			throw new ServiceException("SQLException while getAllOrders");
		}
		List<OrderRequestDTO> allOrders = new ArrayList<OrderRequestDTO>(orderMap.values());
		return allOrders;
	}

}
