package com.highpowerbear.hpbanalytics.model;

import com.highpowerbear.hpbanalytics.database.Execution;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.ib.client.Types;

/**
 * Created by robertk on 10/23/2020.
 */
public class ExecutionContract {

    private final Execution execution;

    private ExecutionContract(Execution execution) {
        this.execution = execution;
    }

    public static ExecutionContract forExecution(Execution e) {
        return new ExecutionContract(e);
    }

    public static String cid(Execution e) {
        return e.getSymbol() + "_" + e.getCurrency() + "_" + e.getSecType() + "_" + String.valueOf(e.getMultiplier()).replace(".", "_");
    }

    public Execution execution() {
        return execution;
    }

    public String symbol() {
        return execution.getSymbol();
    }

    public Currency currency() {
        return execution.getCurrency();
    }

    public Types.SecType secType() {
        return execution.getSecType();
    }

    public double multiplier() {
        return execution.getMultiplier();
    }

    @Override
    public String toString() {
        return cid(execution);
    }
}
