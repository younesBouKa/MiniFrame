package org.injection.core.listeners;

public interface BeanLifeCycleEvent {
    // to see later
    String preBeanBuilding = "preBeanBuilding";
    String postBeanBuilding = "postBeanBuilding";
    String preConstructorCall = "preConstructorCall";
    String postInstancePreparation = "postInstancePreparation";
    String preFactoryCall = "preFactoryCall";
    String postFactoryCall = "postFactoryCall";
    String preInjectFields = "preInjectFields";
    String preInjectField = "preInjectField";
    String postInjectField = "postInjectField";
    String postInjectFields = "postInjectFields";
    String preInjectMethods = "preInjectMethods";
    String preInjectMethodParams = "preInjectMethodParams";
    String preInjectMethodParam = "preInjectMethodParam";
    String postInjectMethodParam = "postInjectMethodParam";
    String preInjectMethod = "preInjectMethod";
    String postInjectMethod = "postInjectMethod";
    String postInjectMethods = "postInjectMethods";
}
