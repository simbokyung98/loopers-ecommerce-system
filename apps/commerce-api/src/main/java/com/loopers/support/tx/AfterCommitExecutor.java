package com.loopers.support.tx;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 트랜잭션 커밋 이후에만 작업을 실행해주는 실행기 */
@Component
public class AfterCommitExecutor {
    public void run(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { task.run(); }
            });
        } else {
            // 트랜잭션이 없으면 즉시 실행
            task.run();
        }
    }
}
