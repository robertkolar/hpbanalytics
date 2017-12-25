package com.highpowerbear.hpbanalytics.iblogger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.enums.IbConnectionType;
import com.ib.client.EClientSocket;
import org.slf4j.LoggerFactory;

/**
 * Created by robertk on 4/4/2015.
 */
public class IbConnection {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(IbConnection.class);

    private IbConnectionType type;
    private String host;
    private Integer port;
    private Integer clientId;
    private String accounts; // csv, filled upon connection to IB, main account + FA subaccounts if any
    private boolean markConnected = false;
    @JsonIgnore
    private EClientSocket eClientSocket; // null means not connected yet or manually disconnected

    public IbConnection() {
    }

    public IbConnection(IbConnectionType type, String host, Integer port, Integer clientId, EClientSocket eClientSocket) {
        this.type = type;
        this.host = host;
        this.port = port;
        this.clientId = clientId;
        this.eClientSocket = eClientSocket;
    }

    private String print() {
        return "mkt data, host=" + host + ", port=" + port + ", clientId=" + clientId;
    }

    public void connect() {
        if (eClientSocket == null) {
            return;
        }
        this.markConnected = true;
        if (!isConnected()) {
            log.info("Connecting " + print());
            eClientSocket.eConnect(host, port, clientId);
            CoreUtil.waitMilliseconds(1000);
            if (isConnected()) {
                log.info("Sucessfully connected " + print());
            }
        }
    }

    public void disconnect() {
        if (eClientSocket == null) {
            return;
        }
        this.markConnected = false;
        if (isConnected()) {
            log.info("Disconnecting " + print());
            eClientSocket.eDisconnect();
            CoreUtil.waitMilliseconds(1000);
            if (!isConnected()) {
                log.info("Successfully disconnected " + print());
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

    public IbConnectionType getType() {
        return type;
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
