package org.demo.services;

import org.demo.dao.OrderDAO;

import java.util.List;

public interface OrderService {

    OrderDAO getOrder(String orderId);
    List<OrderDAO> getOrdersByUser(String userId);

    OrderDAO saveOrder(OrderDAO orderDAO);
    boolean deleteOrder(String orderId);
}
