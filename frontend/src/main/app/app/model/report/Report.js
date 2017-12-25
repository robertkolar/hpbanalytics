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
        'numExecutions',
        'numTrades',
        'numOpenTrades',
        'numUnderlyings',
        'numOpenUnderlyings',
        {name: 'firstExecutionDate', type: 'date', dateFormat: 'time'},
        {name: 'lastExecutionDate', type: 'date', dateFormat: 'time'}
    ]
});