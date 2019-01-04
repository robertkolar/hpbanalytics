package com.highpowerbear.hpbanalytics.connector;

/**
 * Created by robertk on 1/4/2019.
 */
public interface ConnectionListener {
    default void preConnect(String accountId) {}
    default void postConnect(String accountId) {}
    default void preDisconnect(String accountId) {}
    default void postDisconnect(String accountId) {}
}
