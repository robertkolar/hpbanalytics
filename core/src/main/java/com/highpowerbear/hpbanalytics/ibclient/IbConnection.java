package com.highpowerbear.hpbanalytics.ibclient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by robertk on 4/4/2015.
 */
public class IbConnection {
    private static final Logger log = LoggerFactory.getLogger(IbConnection.class);

    private String host;
    private Integer port;
    private Integer clientId;
    private String accounts; // csv, filled upon connection to IB, main account + FA subaccounts if any
    private boolean markConnected = false;
    @JsonIgnore
    private EClientSocket eClientSocket; // null means not connected yet or manually disconnected
    @JsonIgnore
    private EReaderSignal eReaderSignal;

    public IbConnection() {
    }

    public IbConnection(String host, Integer port, Integer clientId, EClientSocket eClientSocket, EReaderSignal eReaderSignal) {
        this.host = host;
        this.port = port;
        this.clientId = clientId;
        this.eClientSocket = eClientSocket;
        this.eReaderSignal = eReaderSignal;
    }

    private String print() {
        return "host=" + host + ", port=" + port + ", clientId=" + clientId;
    }

    public void connect() {
        if (eClientSocket == null) {
            return;
        }
        this.markConnected = true;

        if (!isConnected()) {
            log.info("connecting " + print());
            eClientSocket.eConnect(host, port, clientId);
            CoreUtil.waitMilliseconds(1000);

            if (isConnected()) {
                log.info("successfully connected " + print());

                final EReader eReader = new EReader(eClientSocket, eReaderSignal);

                eReader.start();
                // an additional thread is created in this program design to empty the messaging queue
                new Thread(() -> {
                    while (eClientSocket.isConnected()) {
                        eReaderSignal.waitForSignal();
                        try {
                            eReader.processMsgs();
                        } catch (Exception e) {
                            log.error("error", e);
                        }
                    }
                }).start();
            }
        }
    }

    public void disconnect() {
        if (eClientSocket == null) {
            return;
        }
        this.markConnected = false;
        if (isConnected()) {
            log.info("disconnecting " + print());
            eClientSocket.eDisconnect();
            CoreUtil.waitMilliseconds(1000);
            if (!isConnected()) {
                log.info("successfully disconnected " + print());
                this.accounts = null;
            }
        }
    }
    @JsonProperty
    public Boolean isConnected() {
        return eClientSocket != null && eClientSocket.isConnected();
    }

    public void setAccounts(String accounts) {
        this.accounts = accounts;
    }

    public String getAccounts() {
        return accounts;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getClientId() {
        return clientId;
    }

    public boolean isMarkConnected() {
        return markConnected;
    }

    public EClientSocket getClientSocket() {
        return eClientSocket;
    }
}
