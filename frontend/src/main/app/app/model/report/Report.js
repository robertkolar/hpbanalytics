/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.model.report.Report', {
    extend: 'HanGui.model.report.Base',

    fields: [
        'origin',
        'reportName',
        'stk',
        'opt',
        'fut',
        'fx',
        'cfd',
        {name: 'reportInfo', persist: false},
        {name: 'numExecutions', mapping: 'reportInfo.numExecutions', persist: false},
        {name: 'numTrades', mapping: 'reportInfo.numTrades', persist: false},
        {name: 'numOpenTrades', mapping: 'reportInfo.numOpenTrades', persist: false},
        {name: 'numUnderlyings', mapping: 'reportInfo.numUnderlyings', persist: false},
        {name: 'numOpenUnderlyings', mapping: 'reportInfo.numOpenUnderlyings', persist: false},
        {name: 'firstExecutionDate', mapping: 'reportInfo.firstExecutionDate', type: 'date', dateFormat: 'time', persist: false},
        {name: 'lastExecutionDate', mapping: 'reportInfo.lastExecutionDate', type: 'date', dateFormat: 'time', persist: false}
    ]
});