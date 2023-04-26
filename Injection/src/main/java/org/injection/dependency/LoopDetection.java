package org.injection.dependency;

import org.injection.enums.DependencyType;
import org.injection.enums.ResolutionStatus;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.injection.enums.ResolutionStatus.START;

public class LoopDetection {
    private static final Log logger = Log.getInstance(LoopDetection.class);
    private final List<DependencyLogLine> resolutionLogs;
    private final LinkedList<Dependency> beanCreationChain;

    public LoopDetection(){
        beanCreationChain = new LinkedList<>();
        resolutionLogs = new ArrayList<DependencyLogLine>();
    }

    public void init(){
        beanCreationChain.clear();
        resolutionLogs.clear();
    }

    public void addDependency(DependencyType dependencyType, Class secondClass){
        Class lastClass = getLast()!=null
                ? getLast().getSecondDependency()
                : secondClass;
        if(containsDependency(secondClass)){
            rejectDependency(lastClass, dependencyType, secondClass);
            throw new FrameworkException("Dependency injection loop detected " +
                    "between: ["+lastClass.getCanonicalName()+"] and ["+secondClass.getCanonicalName()+"]"
            );
        }else{
            addDependency(lastClass, dependencyType, secondClass);
        }
    }

    public void resolveDependency(Class clazz, Object fromWhere){
        removeLastDependency(clazz, fromWhere);
    }

    public void rejectDependency(Class clazz, Object fromWhere){
        if(beanCreationChain.size()>0){
            Dependency lastDependency = beanCreationChain.getLast();
            if(clazz.equals(lastDependency.getSecondDependency())){
                Dependency rejectedDependency = new Dependency(
                        lastDependency.getFirstDependency(),
                        lastDependency.getDependencyType(),
                        lastDependency.getSecondDependency()
                );
                log(rejectedDependency, ResolutionStatus.NOT_FOUND, fromWhere);
                beanCreationChain.removeLast();
            }else{
                logger.error(clazz.getCanonicalName()+" is not the last dependency");
            }
        }else {
            throw new FrameworkException("No dependency to reject");
        }
    }

    public void print(){
        printDependencyChainLogs();
    }

    public void clear(){
        beanCreationChain.clear();
        resolutionLogs.clear();
    }

    /*------------------ inner methods -----------------------*/
    private boolean containsDependency(Class clazz){
        for (Dependency dependency : beanCreationChain){
            if(dependency.getFirstDependency().equals(clazz) || dependency.getSecondDependency().equals(clazz))
                return true;
        }
        return false;
    }

    private Dependency getLast(){
        return beanCreationChain.size()>0
                ? beanCreationChain.getLast()
                : null;
    }

    private void addDependency(Class firstClass, DependencyType dependencyType, Class secondClass){
        Dependency dependency = new Dependency(firstClass, dependencyType, secondClass);
        ResolutionStatus resolutionStatus = ResolutionStatus.SEARCHING;
        if(beanCreationChain.size()==0 &&  // first dependency
                dependencyType!=null
                && dependencyType.equals(DependencyType.FIRST)
        ){
            resolutionStatus = START;

        }
        beanCreationChain.add(dependency);
        log(dependency, resolutionStatus, null);
    }

    private void rejectDependency(Class firstClass, DependencyType dependencyType, Class secondClass){
        Dependency dependency = new Dependency(firstClass, dependencyType, secondClass);
        log(dependency, ResolutionStatus.REJECTED, null);
    }

    private void removeLastDependency(Class clazz, Object fromWhere){
        if(beanCreationChain.size()>0){
            Dependency lastDependency = beanCreationChain.getLast();
            if(clazz.equals(lastDependency.getSecondDependency())){
                Dependency removedDependency = beanCreationChain.removeLast();
                log(removedDependency, ResolutionStatus.RESOLVED, fromWhere);
            }else{
                throw new FrameworkException(clazz.getCanonicalName()+" is not the last dependency");
            }
        }else {
            throw new FrameworkException("No dependency to remove");
        }
    }

    private void log(Dependency dependency, ResolutionStatus resolutionStatus, Object fromWhere){
        resolutionLogs.add(new DependencyLogLine(dependency, resolutionStatus, fromWhere));
    }

    private void printDependencyChainLogs(){
        Class beanResolutionClass = resolutionLogs.size()>0
                && resolutionLogs.get(0)!=null
                && resolutionLogs.get(0).getDependency()!=null
                && resolutionLogs.get(0).getDependency().getFirstDependency()!=null
                ? resolutionLogs.get(0).getDependency().getFirstDependency()
                : Void.class;
        logger.debug("--------- Dependency resolution logs ["+beanResolutionClass.getCanonicalName()+"]------------------------");
        for (int i=0; i< resolutionLogs.size(); i++){
            logger.debug(resolutionLogs.get(i));
        }
        logger.debug("------------------------------------------------------------------------------------------");
    }
}
