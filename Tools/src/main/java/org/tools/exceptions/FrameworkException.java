package org.tools.exceptions;

import org.tools.Log;

public class FrameworkException extends RuntimeException {
    private static final Log logger = Log.getInstance(FrameworkException.class);

    public FrameworkException(String message) {
        super(message);
        logger.error(message);
    }

    public FrameworkException(Throwable throwable) {
        super("Unknown exception: ", throwable);
        logger.error(throwable);
    }

}