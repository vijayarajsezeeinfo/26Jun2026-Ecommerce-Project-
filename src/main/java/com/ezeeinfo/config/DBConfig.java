package com.ezeeinfo.config;

import com.mysql.cj.jdbc.MysqlDataSource;

public class DBConfig {
	private static MysqlDataSource dataSource;

	public DBConfig() {

	}

	public static synchronized MysqlDataSource getInstance() {

		if (dataSource == null) {
			dataSource = new MysqlDataSource();
			dataSource.setUrl("jdbc:mysql://localhost:3306/ecommerce_services");
			dataSource.setUser("root");
			dataSource.setPassword("root");

			System.out.println("DATA SOURCE OBJECT CREATED");
		}

		return dataSource;
	}
}
