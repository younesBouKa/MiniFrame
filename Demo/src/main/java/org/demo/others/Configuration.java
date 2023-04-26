package org.demo.others;

import org.demo.services.DBService;
import org.demo.services.ProductService;
import org.demo.services.UserService;
import org.demo.services.impls.ProductServiceImpl;
import org.demo.services.impls.UserServiceImpl;
import org.injection.annotations.Alternative;
import org.injection.annotations.AlternativeConfig;
import org.injection.annotations.Component;
import org.injection.annotations.qualifiers.markers.FirstFound;
import org.injection.core.data.AlternativeInstance;
import org.injection.enums.BeanSourceType;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

public class Configuration {

    @Alternative
    @Singleton
    public UserService getUserServiceBean(@FirstFound DBService dbService, ProductService productService){
        UserServiceImpl userService =  new UserServiceImpl(dbService);
        userService.setProductService(productService);
        return userService;
    }

    @Singleton
    //@FlagQualifier
    public ProductService getProductServiceBean(@FlagQualifier DBService dbService){
        ProductService productService =  new ProductServiceImpl(dbService);
        return productService;
    }

    @AlternativeConfig
    public Set<AlternativeInstance> getAlternatives(){
        Set<AlternativeInstance> alternatives = new HashSet<>();
        alternatives.add(new AlternativeInstance(UserService.class, "org.demo.others.Configuration.getUserServiceBean", BeanSourceType.METHOD));
        //alternatives.add(new AlternativeInstance(UserService.class, UserServiceImpl.class, BeanSourceType.CLASS));
        return alternatives;
    }
}
