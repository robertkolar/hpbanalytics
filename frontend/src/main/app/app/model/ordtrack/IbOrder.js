/**
 * Created by robertk on 4/11/2015.
 */
Ext.define('HanGui.model.ordtrack.IbOrder', {
    extend: 'HanGui.model.ordtrack.Base',

    fields: [
        {name: 'id', type: 'string'},
        'permId',
        'orderId',
        'clientId',
        'action',
        'quantity',
        'underlying',
        'currency',
        'symbol',
        'secType',
        'orderType',
        {name: 'submitDate', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
        'orderPrice',
        'tif',
        'parentId',
        'ocaGroup',
        {name: 'statusDate', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
        'fillPrice',
        'status',
        'heartbeatCount',
        'ibAccountId'
    ]
});