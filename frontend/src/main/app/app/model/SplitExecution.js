/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.model.SplitExecution', {
    extend: 'HanGui.model.Base',

    fields: [
        'splitQuantity',
        'currentPosition',
        'fillDate',
        {name: 'tradeId', type: 'string', reference: {type: 'Trade', inverse: 'splitExecutions'}},
        'executionDisplay'
    ]
});
