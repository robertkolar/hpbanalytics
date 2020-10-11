/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.model.Execution', {
    extend: 'HanGui.model.Base',

    fields: [
        'reference',
        'action',
        'quantity',
        'conid',
        'symbol',
        'underlying',
        'currency',
        'secType',
        'multiplier',
        'fillDate',
        'fillPrice',
        'receivedDate'
    ]
});
