package org.aspect.processor;

import org.aspect.scanners.AspectScanManagerImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class ProxyEventHandler{
    private static final List<AdviceProcessor> adviceProcessorList ;

    static {
        adviceProcessorList = new ArrayList<>();
        adviceProcessorList.add(new DefaultAdviceProcessor(new AspectScanManagerImpl()));
    }

    public static void addAdviceProcessor(AdviceProcessor adviceProcessor){
        adviceProcessorList.add(adviceProcessor);
    }

    public static boolean removeAdviceProcessor(AdviceProcessor adviceProcessor){
        return adviceProcessorList.remove(adviceProcessor);
    }

    public static void execBeforeCall(Object targetInstance, Method method, Object[] args) {
        for (AdviceProcessor adviceProcessor: adviceProcessorList){
            adviceProcessor.execBeforeCall(targetInstance, method, args);
        }
    }

    public static void execAfterCall(Object targetInstance, Method method, Object[] args, Object returnVal) {
        for (AdviceProcessor adviceProcessor: adviceProcessorList){
            adviceProcessor.execAfterCall(targetInstance, method, args, returnVal);
        }
    }

    public static Object execBeforeReturn(Object targetInstance, Method method, Object[] args, Object returnVal) {
        Object returnValNew = returnVal;
        for (AdviceProcessor adviceProcessor: adviceProcessorList){
            returnValNew = adviceProcessor.execBeforeReturn(targetInstance, method, args, returnValNew);
        }
        return returnValNew;
    }

    public static void execOnException(Object targetInstance, Method method, Object[] args, Throwable throwable) {
        for (AdviceProcessor adviceProcessor: adviceProcessorList){
            adviceProcessor.execOnException(targetInstance, method, args, throwable);
        }
    }
}
