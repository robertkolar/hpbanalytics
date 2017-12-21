/**
 * Created by robertk on 9/6/15.
 */
Ext.define('HanGui.view.report.ReportModel', {
    extend: 'Ext.app.ViewModel',
    requires: [
        'HanGui.model.report.Report',
        'HanGui.model.report.Execution',
        'HanGui.model.report.SplitExecution',
        'HanGui.model.report.Trade',
        'HanGui.model.report.Statistics'
    ],

    alias: 'viewmodel.han-report',

    stores: {
        reports: {
            model: 'HanGui.model.report.Report',
            pageSize: 10
        },
        executions: {
            model: 'HanGui.model.report.Execution',
            pageSize: 25,
            remoteFilter: true,
            remoteSort: false
        },
        trades: {
            model: 'HanGui.model.report.Trade',
            pageSize: 25,
            remoteFilter: true,
            remoteSort: false
        },
        statistics: {
            model: 'HanGui.model.report.Statistics',
            pageSize: 10
        },
        charts: {
            model: 'HanGui.model.report.Chart'
        }
    }
});