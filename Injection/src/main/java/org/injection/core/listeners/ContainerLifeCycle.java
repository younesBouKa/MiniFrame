package org.injection.core.listeners;

public interface ContainerLifeCycle {
    String postRemoveBeans = "postRemoveBeans";
    String preRemoveBeans = "postRemoveBeans";
    String postRemoveBean = "postRemoveBeans";
    String preRemoveBean = "postRemoveBeans";
    String preAddBean = "postRemoveBeans";
    String postAddBean = "postRemoveBeans";

    default void onEvent(String eventType, Object ...args){

    }
}
