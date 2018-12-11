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

    private final String host;
    private final Integer port;
    private final Integer clientId;
    private boolean markConnected = false;
    @JsonIgnore
    private final EClientSocket eClientSocket; // null means not connected yet or manually disconnected
    @JsonIgnore
    private final EReaderSignal eReaderSignal;

    public IbConnection(String host, Integer port, Integer clientId, EClientSocket eClientSocket, EReaderSignal eReaderSignal) {
        this.host = host;
        this.port = port;
        this.clientId = clientId;
        this.eClientSocket = eClientSocket;
        this.eReaderSignal = eReaderSignal;
    }

    public void connect() {
        if (eClientSocket == null) {
            return;
        }
        this.markConnected = true;

        if (!isConnected()) {
            log.info("connecting " + getInfo());
            eClientSocket.eConnect(host, port, clientId);
            CoreUtil.waitMilliseconds(1000);

            if (isConnected()) {
                log.info("successfully connected " + getInfo());

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
            log.info("disconnecting " + getInfo());
            eClientSocket.eDisconnect();
            CoreUtil.waitMilliseconds(1000);
            if (!isConnected()) {
                log.info("successfully disconnected " + getInfo());
            }
        }
    }

    public String getInfo() {
        return "host=" + host + ", port=" + port + ", clientId=" + clientId;
    }

    @JsonProperty
    public Boolean isConnected() {
        return eClientSocket != null && eClientSocket.isConnected();
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
