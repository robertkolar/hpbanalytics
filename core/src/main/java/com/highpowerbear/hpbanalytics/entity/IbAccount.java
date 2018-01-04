package com.highpowerbear.hpbanalytics.entity;

import com.highpowerbear.hpbanalytics.ibclient.IbConnection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Created by robertk on 5/29/2017.
 */
@Entity
@Table(name = "ib_account", schema = "hpbanalytics", catalog = "hpbanalytics")
public class IbAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String accountId;
    private String host;
    private Integer port;
    private boolean listen;
    private boolean allowUpd;
    private boolean stk;
    private boolean fut;
    private boolean opt;
    private boolean fx;
    private boolean cfd;
    private String permittedClients; // csv, null means all
    private String permittedAccounts; // csv, null means all - applicable to FA subaccounts
    @Transient
    private IbConnection ibConnection;

    public boolean mayProcessClient(Integer clientId) {
        if (permittedClients == null || permittedClients.trim().isEmpty()) {
            return true;
        }
        boolean mayProcess = false;
        String[] cs = permittedClients.split(",");
        for(String c : cs) {
            if (Integer.parseInt(c.trim()) == clientId) {
                mayProcess = true;
                break;
            }
        }
        return mayProcess;
    }

    public boolean mayProcessAccount(String faSubaccount) {
        if (permittedAccounts == null || permittedAccounts.trim().isEmpty()) {
            return true;
        }
        boolean mayProcess = false;
        String[] as = permittedAccounts.split(",");
        for(String a : as) {
            if (a.trim().equalsIgnoreCase(faSubaccount)) {
                mayProcess = true;
                break;
            }
        }
        return mayProcess;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IbAccount ibAccount = (IbAccount) o;

        return !(accountId != null ? !accountId.equals(ibAccount.accountId) : ibAccount.accountId != null);

    }

    @Override
    public int hashCode() {
        return accountId != null ? accountId.hashCode() : 0;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean isListen() {
        return listen;
    }

    public void setListen(boolean listen) {
        this.listen = listen;
    }

    public boolean isAllowUpd() {
        return allowUpd;
    }

    public void setAllowUpd(boolean allowUpd) {
        this.allowUpd = allowUpd;
    }

    public boolean isStk() {
        return stk;
    }

    public void setStk(boolean stk) {
        this.stk = stk;
    }

    public boolean isFut() {
        return fut;
    }

    public void setFut(boolean fut) {
        this.fut = fut;
    }

    public boolean isOpt() {
        return opt;
    }

    public void setOpt(boolean opt) {
        this.opt = opt;
    }

    public boolean isFx() {
        return fx;
    }

    public void setFx(boolean fx) {
        this.fx = fx;
    }

    public boolean isCfd() {
        return cfd;
    }

    public void setCfd(boolean cfd) {
        this.cfd = cfd;
    }

    public String getPermittedClients() {
        return permittedClients;
    }

    public void setPermittedClients(String permittedClients) {
        this.permittedClients = permittedClients;
    }

    public String getPermittedAccounts() {
        return permittedAccounts;
    }

    public void setPermittedAccounts(String permittedAccounts) {
        this.permittedAccounts = permittedAccounts;
    }

    public IbConnection getIbConnection() {
        return ibConnection;
    }

    public void setIbConnection(IbConnection ibConnection) {
        this.ibConnection = ibConnection;
    }
}
