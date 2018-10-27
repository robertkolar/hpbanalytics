/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.model.report.Execution', {
    extend: 'HanGui.model.report.Base',

    fields: [
        {name: 'receivedDate', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
        'comment',
        'origin',
        'referenceId',
        'action',
        'quantity',
        'symbol',
        'underlying',
        'currency',
        'secType',
        {name: 'fillDate', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
        'fillPrice',
        'reportId'
    ]
});