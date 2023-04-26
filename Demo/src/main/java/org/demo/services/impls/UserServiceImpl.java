package org.demo.services.impls;

import org.demo.dao.UserDAO;
import org.demo.others.ClassNameQualifier;
import org.demo.services.ProductService;
import org.demo.services.UserService;
import org.demo.services.DBService;
import org.injection.annotations.Alternative;
import org.injection.annotations.qualifiers.markers.EvalWithAND;
import org.injection.annotations.qualifiers.markers.ElseFirstFound;
import org.injection.annotations.qualifiers.RegexQualifier;
import org.injection.annotations.qualifiers.markers.FirstFound;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

//@Component
@Singleton
@Alternative
public class UserServiceImpl implements UserService {

    private DBService dbService;
    private ProductService productService;

    @Inject
    public UserServiceImpl(
            //@RegexQualifier(regex = ".*(_2)")
            //@NameQualifier(name = "toto")
            //@EvalWithAND
            //@EvalWithOR
            //@ElseFirstFound
            @Named(value = "defaultDB")
            DBService dbService) {
        this.dbService = dbService;
    }

    @Override
    public UserDAO login(String userId) {
        return dbService.getUserById(userId);
    }

    @Override
    public UserDAO register(UserDAO userDAO) {
        return dbService.saveUser(userDAO);
    }

    @Override
    public boolean deleteUser(String userId) {
        return dbService.deleteUser(userId);
    }

    @Override
    public UserDAO updateUserInfo(UserDAO userDAO) {
        return dbService.saveUser(userDAO);
    }

    @Inject
    public void setProductService(@FirstFound ProductService productService){
        this.productService = productService;
    }
}
