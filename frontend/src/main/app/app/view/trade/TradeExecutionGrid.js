/**
 * Created by robertk on 10/19/2015.
 */
Ext.define('HanGui.view.trade.TradeExecutionGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'han-trade-execution-grid',
    requires: [
        'Ext.grid.column.Date'
    ],
    viewConfig: {
        stripeRows: true
    },
    columns: [{
        text: 'ID',
        width: 80,
        dataIndex: 'id'
    }, {
        text: 'Action',
        width: 60,
        dataIndex: 'action',
        renderer: 'actionRenderer'
    }, {
        text: 'Qnt',
        width: 80,
        dataIndex: 'quantity',
        align: 'right'
    }, {
        text: 'Symbol',
        width: 180,
        dataIndex: 'symbol',
        filter: 'string'
    }, {
        text: 'Sec',
        width: 60,
        dataIndex: 'secType'
    }, {
        text: 'Cur',
        width: 60,
        dataIndex: 'currency'
    }, {
        text: 'Mul',
        width: 60,
        dataIndex: 'multiplier',
        align: 'right'
    }, {
        text: 'Fill Date',
        flex: 1,
        dataIndex: 'fillDate',
        xtype: 'datecolumn',
        format: 'm/d/Y H:i:s'
    }, {
        text: 'Fill',
        width: 100,
        dataIndex: 'fillPrice',
        align: 'right',
        renderer: 'priceRenderer'
    }, {
        text: 'Value',
        width: 120,
        dataIndex: 'value',
        align: 'right',
        renderer: 'priceRenderer'
    }]
});
