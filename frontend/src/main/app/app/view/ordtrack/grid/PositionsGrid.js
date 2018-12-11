/**
 * Created by robertk on 12/29/2017.
 */
Ext.define('HanGui.view.ordtrack.grid.PositionsGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'han-ordtrack-positions-grid',

    requires: [
        'Ext.grid.column.Date',
        'Ext.toolbar.Paging',
        'HanGui.view.ordtrack.OrdTrackController'
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
        text: 'Sec',
        width: 100,
        dataIndex: 'secType'
    }, {
        text: 'Undl',
        width: 80,
        dataIndex: 'underlyingSymbol'
    }, {
        text: 'Symbol',
        width: 180,
        dataIndex: 'symbol'
    }, {
        text: 'Cur',
        width: 60,
        dataIndex: 'currency'
    }, {
        text: 'Exchange',
        width: 100,
        dataIndex: 'exchange'
    }, {
        text: 'Pos',
        width: 80,
        dataIndex: 'size',
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