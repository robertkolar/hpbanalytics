/**
 * Created by robertk on 12/29/2017.
 */
Ext.define('HanGui.view.iblogger.grid.PositionsGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'han-iblogger-positions-grid',

    requires: [
        'Ext.grid.column.Date',
        'Ext.toolbar.Paging',
        'HanGui.view.iblogger.IbLoggerController'
    ],
    bind: '{positions}',
    viewConfig: {
        stripeRows: true
    },
    columns: [{
        text: 'Account ID',
        width: 120,
        dataIndex: 'accountId'
    }, {
        text: 'Symbol',
        width: 180,
        dataIndex: 'symbol'
    }, {
        text: 'Undl',
        width: 80,
        dataIndex: 'underlying'
    }, {
        text: 'Cur',
        width: 60,
        dataIndex: 'currency'
    }, {
        text: 'Sec',
        width: 60,
        dataIndex: 'secType'
    }, {
        text: 'Pos',
        width: 80,
        dataIndex: 'position',
        align: 'right'
    }, {
        flex: 1
    }],
    dockedItems: [{
        xtype: 'pagingtoolbar',
        reference: 'positionsPaging',
        bind: '{positions}',
        dock: 'bottom',
        displayInfo: true
    }]
});