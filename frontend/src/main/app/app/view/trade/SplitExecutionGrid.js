/**
 * Created by robertk on 10/19/2015.
 */
Ext.define('HanGui.view.trade.SplitExecutionGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'han-splitexecution-grid',
    requires: [
        'Ext.grid.column.Date'
    ],
    viewConfig: {
        stripeRows: true
    },
    columns: [{
        xtype: 'templatecolumn',
        text: 'ID',
        width: 100,
        tpl: '{tradeId}/{id}'
    }, {
        text: 'Split Q',
        width: 80,
        dataIndex: 'splitQuantity',
        align: 'right'
    }, {
        text: 'Cur Pos',
        width: 80,
        dataIndex: 'currentPosition',
        align: 'right'
    }, {
        text: 'Fill Date',
        width: 160,
        dataIndex: 'fillDate',
        xtype: 'datecolumn',
        format: 'm/d/Y H:i:s'
    }, {
        text: 'Execution',
        flex: 1,
        dataIndex: 'executionDisplay'
    }]
});
