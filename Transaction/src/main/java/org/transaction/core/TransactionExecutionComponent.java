package org.transaction.core;

import org.tools.Log;

public class TransactionExecutionComponent {
    private static final Log logger = Log.getInstance(TransactionExecutionComponent.class);

    /**
     * Begin a sub transaction
     * @return transactionId
     */
    public String beginTransaction(){
        logger.info("beginTransaction");
        // create transaction id
        return "";
    }

    /**
     * Abort sub transaction
     */
    public void abortTransaction(){
        logger.info("abortTransaction");
        // abort current transaction
    }

    /**
     * End sub transaction
     * @param compensatingTrId: compensating transaction id
     */
    public void endTransaction(String compensatingTrId){
        logger.info("endTransaction {"+compensatingTrId+"}");
        // commit transaction
    }

}
