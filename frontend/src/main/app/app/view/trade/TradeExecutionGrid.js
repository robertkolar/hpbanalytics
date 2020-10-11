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
        width: 100,
        dataIndex: 'id'
    }, {
        text: 'Action',
        width: 60,
        dataIndex: 'action',
        renderer: function(val, metadata, record) {
            metadata.style = (val === 'BUY' ? 'color: blue;' : 'color: brown;');
            return val;
        }
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
        renderer: function(val, metadata, record) {
            return Ext.util.Format.number(val, '0.00###');
        }
    }]
});
