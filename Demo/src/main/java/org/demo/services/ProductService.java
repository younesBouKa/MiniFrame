package org.demo.services;

import org.demo.dao.ProductDAO;

public interface ProductService {

    ProductDAO getProduct(String productId);
    ProductDAO saveProduct(ProductDAO productDAO);
    boolean deleteProduct(String productId);
}
