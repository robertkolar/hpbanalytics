/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.model.report.SplitExecution', {
    extend: 'HanGui.model.report.Base',

    fields: [
        'splitQuantity',
        'currentPosition',
        {name: 'fillDate', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
        {name: 'tradeId', type: 'string', reference: {type: 'Trade', inverse: 'splitExecutions'}},
        'executionDisplay'
    ]
});