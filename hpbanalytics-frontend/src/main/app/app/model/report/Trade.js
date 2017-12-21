/**
 * Created by robertk on 9/6/15.
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
        {name: 'openDate', type: 'date', dateFormat: 'time'},
        'avgClosePrice',
        {name: 'closeDate', type: 'date', dateFormat: 'time'},
        'duration',
        'profitLoss',
        'reportId'
    ]
});