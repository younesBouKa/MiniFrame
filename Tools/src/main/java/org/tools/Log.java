package org.tools;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {

    private Class<?> aClass;
    private Logger logger;

    private Log(Class<?> aClass){
        this.aClass = aClass;
        this.logger = LogManager.getLogger(aClass);
    }

    public static Log getInstance(Class<?> clazz){
        return new Log(clazz);
    }

    /*------------- info ---------------------*/
    public void info(Object msg){
        logger.info(msg);
    }
    public void info(String msg){
        logger.info(msg);
    }
    public void info(String msg, Object... args){
        logger.info(msg, args);
    }
    public void infoF(String msg, Object... args){
        info(String.format(msg, args));
    }


    /*------------- debug ---------------------*/
    public void debug(Object msg){
        logger.debug(msg);
    }
    public void debug(String msg){
        logger.debug(msg);
    }
    public void debug(String msg, Object... args){
        logger.debug(msg, args);
    }
    public void debugF(String msg, Object... args){
        debug(String.format(msg, args));
    }

    /*------------- error ---------------------*/
    public void error(Object msg){
        logger.error(msg);
    }
    public void error(String msg){
        logger.error(msg);
    }
    public void error(String msg, Object... args){
        logger.error(msg, args);
    }
    public void errorF(String msg, Object... args){
        error(String.format(msg, args));
    }
    public void error(Throwable throwable){
        logger.throwing(Level.WARN, throwable);
    }
    /*------------- warning ---------------------*/
    public void warn(Object msg){
        logger.warn(msg);
    }
    public void warn(String msg){
        logger.warn(msg);
    }
    public void warn(String msg, Object... args){
        logger.warn(msg, args);
    }
    public void warnF(String msg, Object... args){
        warn(String.format(msg, args));
    }
    public void warn(Throwable throwable){
        logger.throwing(Level.WARN, throwable);
    }
}
