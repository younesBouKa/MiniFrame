package org.injection.core.listeners;

public interface BeanLifeCycleEventParams {
    // to see later
    String INSTANCE = "INSTANCE";
    String BEAN_TYPE = "BEAN_TYPE";
    String FIELD = "FIELD";
    String FIELDS = "FIELDS";
    String METHOD = "METHOD";
    String METHODS = "METHODS";
    String PROCESSED_METHODS = "PROCESSED_METHODS";
    String QUALIFIERS = "QUALIFIERS";
    String SCOPE_ID = "SCOPE_ID";
    String SCOPE_TYPE = "SCOPE_TYPE";
    String SCOPE_INSTANCES = "SCOPE_INSTANCES";
    String IMPLEMENTATION = "IMPLEMENTATION";
    String CONSTRUCTOR = "CONSTRUCTOR";
    String CONSTRUCTOR_PARAMETERS = "CONSTRUCTOR_PARAMETERS";
    String FACTORY = "FACTORY";
    String FACTORY_PARAMETERS = "FACTORY_PARAMETERS";
}
