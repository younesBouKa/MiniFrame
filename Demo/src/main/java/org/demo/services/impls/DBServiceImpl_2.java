package org.demo.services.impls;

import org.demo.dao.OrderDAO;
import org.demo.dao.ProductDAO;
import org.demo.dao.UserDAO;
import org.demo.others.FlagQualifier;
import org.demo.services.DBService;
import org.injection.annotations.Alternative;
import org.injection.others.TestBean;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;

import javax.inject.Named;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestBean
@Alternative
@FlagQualifier
public class DBServiceImpl_2 implements DBService {
    private static final Log logger = Log.getInstance(DBServiceImpl_2.class);
    private static Map<Class,String> dataFiles ;
    private HashMap<String, UserDAO> users = new HashMap<>();
    private HashMap<String,OrderDAO> orders = new HashMap<>();
    private HashMap<String,ProductDAO> products = new HashMap<>();

    static {
        dataFiles = new HashMap<>();
        dataFiles.put(UserDAO.class, "usersDB");
        dataFiles.put(ProductDAO.class, "productsDB");
        dataFiles.put(OrderDAO.class, "ordersDB");
    }

    public DBServiceImpl_2(){
        populateDB();
    }

    @Override
    public UserDAO getUserById(String userId) {
        return users.get(userId);
    }

    @Override
    public UserDAO saveUser(UserDAO userDAO) {
        if(userDAO.getId()==null)
            userDAO.setId(users.size()+"");
        users.put(userDAO.getId(), userDAO);
        commit();
        return userDAO;
    }

    @Override
    public boolean deleteUser(String userId) {
        UserDAO userDAO = users.remove(userId);
        if(userDAO!=null)
            commit();
        return userDAO!=null;
    }

    @Override
    public ProductDAO getProductById(String productId) {
        return products.get(productId);
    }

    @Override
    public ProductDAO saveProduct(ProductDAO productDAO) {
        if(productDAO.getId()==null)
            productDAO.setId(products.size()+"");
        products.put(productDAO.getId(), productDAO);
        commit();
        return productDAO;
    }

    @Override
    public boolean deleteProduct(String productId) {
        ProductDAO productDAO = products.remove(productId);
        if(productDAO!=null)
            commit();
        return productDAO!=null;
    }

    @Override
    public OrderDAO getOrderById(String orderId) {
        return orders.get(orderId);
    }

    @Override
    public OrderDAO saveOrder(OrderDAO orderDAO) {
        if(orderDAO.getId()==null)
            orderDAO.setId(orders.size()+"");
        orders.put(orderDAO.getId(), orderDAO);
        commit();
        return orderDAO;
    }

    @Override
    public boolean deleteOrder(String orderId) {
        OrderDAO orderDAO = orders.remove(orderId);
        if(orderDAO!=null)
            commit();
        return orderDAO!=null;
    }

    /*-----------------------------------------------------------------------------------------*/
    private void populateDB(){
        if(true)
            return;
        // populating users
        users.clear();
        for(Object obj : populate(UserDAO.class)){
            UserDAO userDAO = (UserDAO) obj;
            users.put(userDAO.getId(), userDAO);
        }
        // populating orders
        orders.clear();
        for(Object obj : populate(OrderDAO.class)){
            OrderDAO orderDAO = (OrderDAO) obj;
            orders.put(orderDAO.getId(), orderDAO);
        }
        // populating products
        products.clear();
        for(Object obj : populate(ProductDAO.class)){
            ProductDAO productDAO = (ProductDAO) obj;
            products.put(productDAO.getId(), productDAO);
        }
    }

    private void commit(){
        if(true)
            return;
        // committing users
        commitObjects(new ArrayList<>(users.values()), UserDAO.class);
        // committing orders
        commitObjects(new ArrayList<>(orders.values()), UserDAO.class);
        // committing products
        commitObjects(new ArrayList<>(products.values()), UserDAO.class);
    }

    private List populate(Class clazz){
        if(!dataFiles.containsKey(clazz)){
            throw new FrameworkException("No data file defined for class: "+clazz.getName());
        }
        String filePath = dataFiles.get(clazz);
        File file = new File(filePath);
        if(!file.exists()){
            logger.error("Data file not exists: "+filePath);
            return new ArrayList();
        }
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))){
            List listObjects = (List<Object>) in.readObject();
            List returnedObjects =new ArrayList<>();
            for (Object obj : listObjects){
                returnedObjects.add(clazz.getClass().cast(obj));
            }
            return returnedObjects;
        }catch (Exception e){
            logger.error("Can't read data from file: "+filePath);
            throw new FrameworkException(e);
        }
    }

    private void commitObjects(List<Object> objects, Class clazz){
        if(objects.isEmpty()){
            logger.error("No objects to store in db files");
        }

        if(!dataFiles.containsKey(clazz)){
            throw new FrameworkException("No data file defined for class: "+clazz.getName());
        }
        String filePath = dataFiles.get(clazz);
        File file = new File(filePath);
        if(!file.exists()){
            try {
                file.setWritable(true);
                file.setReadable(true);
                file.createNewFile();
            }catch (Exception e){
                throw new FrameworkException("Can't create data file "+filePath);
            }
        }
        try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(filePath))){
            objectOutputStream.writeObject(objects);
        }catch (Exception e){
            throw new FrameworkException("Can't write objects to file "+filePath);
        }
    }


}
