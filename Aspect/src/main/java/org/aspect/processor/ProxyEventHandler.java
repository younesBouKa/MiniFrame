package org.aspect.processor;

import org.aspect.proxy.JoinPoint;
import org.aspect.scanners.AspectScanManagerImpl;

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

    public static void execBeforeCall(JoinPoint joinPoint) {
        for (AdviceProcessor adviceProcessor: adviceProcessorList){
            adviceProcessor.execBeforeCall(joinPoint);
        }
    }

    public static void execAfterCall(JoinPoint joinPoint) {
        for (AdviceProcessor adviceProcessor: adviceProcessorList){
            adviceProcessor.execAfterCall(joinPoint);
        }
    }

    public static Object execBeforeReturn(JoinPoint joinPoint) {
        Object returnValNew = joinPoint.getReturnVal();
        for (AdviceProcessor adviceProcessor: adviceProcessorList){
            returnValNew = adviceProcessor.execBeforeReturn(joinPoint);
            joinPoint.setReturnVal(returnValNew);
        }
        return returnValNew;
    }

    public static void execOnException(JoinPoint joinPoint) {
        for (AdviceProcessor adviceProcessor: adviceProcessorList){
            adviceProcessor.execOnException(joinPoint);
        }
    }
}
