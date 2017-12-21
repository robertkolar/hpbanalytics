/**
 * Created by robertk on 9/6/15.
 */
Ext.define('HanGui.model.report.SplitExecution', {
    extend: 'HanGui.model.report.Base',

    fields: [
        'splitQuantity',
        'currentPosition',
        {name: 'fillDate', type: 'date', dateFormat: 'time'},
        {name: 'tradeId', type: 'string', reference: {type: 'Trade', inverse: 'splitExecutions'}},
        'executionDisplay'
    ]
});