package com.highpowerbear.hpbanalytics.entity;

import com.highpowerbear.hpbanalytics.report.model.ReportInfo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Created by robertk on 5/29/2017.
 */
@Entity
@Table(name = "report", schema = "hpbanalytics", catalog = "hpbanalytics")
public class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    private Integer id;
    private String origin; // in case of IB origin --> IB:ibAccountId
    private String reportName;
    private boolean stk;
    private boolean fut;
    private boolean opt;
    private boolean fx;
    private boolean cfd;
    @Transient
    private ReportInfo reportInfo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getReportName() {
        return reportName;
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

    public void setReportName(String name) {
        this.reportName = name;
    }

    public ReportInfo getReportInfo() {
        return reportInfo;
    }

    public void setReportInfo(ReportInfo reportInfo) {
        this.reportInfo = reportInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Report report = (Report) o;

        return !(id != null ? !id.equals(report.id) : report.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
