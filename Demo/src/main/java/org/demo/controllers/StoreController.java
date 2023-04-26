package org.demo.controllers;

import com.sun.net.httpserver.HttpExchange;
import org.demo.dao.OrderDAO;
import org.demo.others.FlagQualifier;
import org.demo.others.ClassNameQualifier;
import org.demo.services.OrderService;
import org.demo.services.ProductService;
import org.injection.annotations.lifecycle.PostInit;
import org.tools.ClassFinder;
import org.tools.Log;
import org.web.annotations.methods.*;
import org.web.annotations.others.Controller;
import org.web.annotations.others.Valid;
import org.web.annotations.params.global.Names;
import org.web.annotations.params.global.Param;
import org.web.annotations.params.global.ParamSrc;
import org.web.annotations.params.global.Source;
import org.web.annotations.params.types.QueryParam;

import javax.inject.Inject;
import java.util.Collections;

@Controller(root = "/store")
public class StoreController{
    private static final Log logger = Log.getInstance(StoreController.class);
    @Inject
    private OrderService orderService;

    @Inject
    @ClassNameQualifier(name="org.demo.services.impls.ProductServiceImpl")
    private ProductService productService;

    @PostInit
    public void checkInstanceInitialization(){
        logger.info("StoreController Instance initialized : "+this);
    }

    // test mixing annotation (get the nearest one to parameter)
    // in this case @QueryParam
    @Get(route = "/orders")
    @Source(src = ParamSrc.BODY)
    @Names(names = {"orderId"})
    public OrderDAO getOrder(
            @Param(name = "id", type = ParamSrc.HEADER)
            @QueryParam(name = "toto")
            @Valid
                String orderId,
                             HttpExchange httpExchange){
        logger.debug("httpExchange: "+httpExchange.getRequestURI());
        //ClassFinder.addToClassPath(Collections.singleton("toto"+System.currentTimeMillis()+".jar"));
        return orderService.getOrder(orderId);
    }


    @Route(route = "/orders", method = HttpMethod.POST)
    @Source(src = ParamSrc.BODY)
    @Names(names={"userId","productId","quantity","unitePrice"})
    public OrderDAO saveOrder(String userId,
                              @Param(name = "product", type=ParamSrc.QUERY) String productId,
                              int quantity,
                              int unitePrice,
                              HttpExchange httpExchange){
        logger.debug("saveOrder path: "+httpExchange.getRequestURI());
        OrderDAO orderDAO = new OrderDAO();
        orderDAO.setProductId(productId);
        orderDAO.setQuantity(quantity);
        orderDAO.setIdUser(userId);
        orderDAO.setUnitePrice(unitePrice);
        return orderService.saveOrder(orderDAO);
    }



    @Put(route = "/orders")
    @Source(src = ParamSrc.BODY)
    @Names(names={"orderId","userId","productId","quantity","unitePrice"})
    public OrderDAO updateOrder(String orderId, String userId, String productId, int quantity, int unitePrice){
        OrderDAO orderDAO = new OrderDAO();
        orderDAO.setId(orderId);
        orderDAO.setProductId(productId);
        orderDAO.setQuantity(quantity);
        orderDAO.setIdUser(userId);
        orderDAO.setUnitePrice(unitePrice);
        return orderService.saveOrder(orderDAO);
    }

    @Delete(route = "/orders")
    public boolean deleteOrder(@QueryParam(name = "id") String orderId){
        return orderService.deleteOrder(orderId);
    }
}
