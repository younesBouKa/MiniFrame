package org.transaction.core;

import org.tools.Log;

public class SagaExecutionComponent {
    private static final Log logger = Log.getInstance(SagaExecutionComponent.class);

    /**
     * Begin a saga transaction
     * @return sagaTransactionId
     */
    public String beginSaga(){
        logger.info("beginSaga");
        // create saga transaction id
        return "";
    }

    /**
     * Abort saga transaction
     * @param savePointId
     */
    public void abortSagaTransaction(String savePointId){
        logger.info("abortSagaTransaction {"+savePointId+"}");
        // go to savepoint
        // start compensating transactions
    }

    // End saga transaction
    public void endSagaTransaction(){
        logger.info("endSagaTransaction");
        // end last transaction
        // end saga
        // need to have a compensatmg transaction for the last transaction
    }

    /**
     * Create save point
     * @return savePointId
     */
    public String createSavePoint(){
        logger.info("createSavePoint");
        /*
        -   This command can be issued between transactions It forces the system to
        save the state of the running application program and returns a save-point identifier for future reference
        -   The save points could then be useful in reducing the amount of work after a saga failure
        or a system crash instead of compensating for all the outstanding transactions,
        -   The system could compensate for transactions executed since the
        last save point, and then restart the saga
        -   Of course, this means that we can now have executions of the type T1, Ts, C’s, Tz, T3, Tq,
        Tg, Cg, Cq, Td, Tg, T~J (After successfully executing T2 the first time, the system crashed
        A save-point had been taken after T1, but to restart here, the system first undoes T2 by running
        C2 Then the saga can be restarted and T2 ré-executed A second failure occurred after the
        execution of T5 ) This means that our definition of valid execution sequences given above must be
        modified to include such sequences
        -   If these partial recovery sequences are not valid, then the
        system should either not take save-points, or it should take them automatically at the beginning
        (or end) of every transaction
         */
        return "";
    }
}
