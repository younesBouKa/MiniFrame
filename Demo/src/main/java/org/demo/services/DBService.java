package org.demo.services;

import org.demo.dao.ProductDAO;
import org.demo.dao.UserDAO;
import org.demo.dao.OrderDAO;

public interface DBService {

    // user methods
    UserDAO getUserById(String userId);
    UserDAO saveUser(UserDAO userDAO);
    boolean deleteUser(String userId);

    // ProductDAO methods
    ProductDAO getProductById(String productId);
    ProductDAO saveProduct(ProductDAO productDAO);
    boolean deleteProduct(String productId);

    // Order methods
    OrderDAO getOrderById(String orderId);
    OrderDAO saveOrder(OrderDAO orderDAO);
    boolean deleteOrder(String orderId);
}
