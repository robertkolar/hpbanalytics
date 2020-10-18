/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.model.Execution', {
    extend: 'HanGui.model.Base',

    fields: [
        'receivedDate',
        'reference',
        'action',
        'quantity',
        'symbol',
        'underlying',
        'currency',
        'secType',
        'multiplier',
        'fillDate',
        'fillPrice',
        {name: 'tradeId', reference: {type: 'Trade', inverse: 'executions'}}
    ]
});
