/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.model.report.Trade', {
    extend: 'HanGui.model.report.Base',

    fields: [
        'type',
        'symbol',
        'underlying',
        'currency',
        'secType',
        'cumulativeQuantity',
        'status',
        'openPosition',
        'avgOpenPrice',
        {name: 'openDate', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
        'avgClosePrice',
        {name: 'closeDate', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
        'duration',
        'profitLoss',
        'reportId'
    ]
});