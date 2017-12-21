/**
 * Created by robertk on 9/6/15.
 */
Ext.define('HanGui.view.report.grid.TradesGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'han-report-trades-grid',
    requires: [
        'Ext.grid.column.Date',
        'Ext.toolbar.Paging',
        'HanGui.view.report.TradesController',
        'Ext.grid.filters.Filters'
    ],
    plugins: 'gridfilters',
    controller: 'han-report-trades',
    bind: '{trades}',
    viewConfig: {
        stripeRows: true
    },
    listeners: {
        'cellclick': 'showSplitExecutions'
    },
    columns: [{
        xtype: 'templatecolumn',
        text: 'ID',
        width: 80,
        tpl: '{reportId}/{id}'
    }, {
        text: 'Open Date',
        width: 180,
        dataIndex: 'openDate',
        xtype: 'datecolumn',
        format: 'm/d/Y H:i:s.u',
        filter: {
            type: 'date',
            dateFormat: 'time'
        }
    }, {
        text: 'Type',
        width: 80,
        dataIndex: 'type',
        renderer: function(val, metadata, record) {
            metadata.style = (val == 'LONG' ? 'color: blue;' : 'color: brown;');
            return val;
        }
    }, {
        text: 'Qnt',
        width: 80,
        dataIndex: 'cumulativeQuantity',
        align: 'right'
    }, {
        text: 'Pos',
        width: 80,
        dataIndex: 'openPosition',
        align: 'right'
    }, {
        text: 'Sec',
        width: 80,
        dataIndex: 'secType',
        filter: {
            type: 'list',
            options: ['STK', 'OPT', 'FUT', 'CASH', 'CFD']
        }
    }, {
        text: 'Undl',
        width: 80,
        dataIndex: 'underlying'
    }, {
        text: 'Cur',
        width: 80,
        dataIndex: 'currency'
    }, {
        text: 'Symbol',
        width: 180,
        dataIndex: 'symbol',
        filter: 'string'
    }, {
        text: 'Open',
        width: 100,
        dataIndex: 'avgOpenPrice',
        align: 'right',
        renderer: function(val, metadata, record) {
            return Ext.util.Format.number(val, '0.00###');
        }
    }, {
        text: 'Close',
        width: 100,
        dataIndex: 'avgClosePrice',
        align: 'right',
        renderer: function(val, metadata, record) {
            return Ext.util.Format.number(val, '0.00###');
        }
    }, {
        text: 'Close Date',
        width: 180,
        dataIndex: 'closeDate',
        xtype: 'datecolumn',
        format: 'm/d/Y H:i:s.u'
    }, {
        text: 'Duration',
        width: 140,
        dataIndex: 'duration'
    }, {
        text: 'P/L',
        width: 100,
        dataIndex: 'profitLoss',
        align: 'right',
        renderer: function(val, metadata, record) {
            metadata.style = val < 0 ? 'color: red;' : 'color: green;';
            return Ext.util.Format.number(val, '0.00');
        }
    }, {
        flex: 1
    }, {
        text: 'Status',
        width: 60,
        dataIndex: 'status',
        renderer: function(val, metadata, record) {
            metadata.style = 'cursor: pointer; color: white; ' + (val == 'OPEN' ? 'background-color: green;' : 'background-color: brown;');
            return val.toLowerCase();
        },
        filter: {
            type: 'list',
            options: ['open', 'closed']
        }
    }, {
        xtype: 'widgetcolumn',
        width : 50,
        widget: {
            xtype: 'button',
            width: 30,
            text: 'C',
            tooltip: 'Close Trade',
            handler: 'onCloseTrade'
        },
        onWidgetAttach: function(col, widget, rec) {
            widget.show();
            if ("OPEN" != rec.data.status) {
                widget.hide();
            }
        }
    }, {
        xtype: 'widgetcolumn',
        width : 50,
        widget: {
            xtype: 'button',
            width: 30,
            text: 'E',
            tooltip: 'Expire Trade (Option)',
            handler: 'onExpireTrade'
        },
        onWidgetAttach: function(col, widget, rec) {
            widget.show();
            if ("OPEN" != rec.data.status || "OPT" != rec.data.secType) {
                widget.hide();
            }
        }
    }, {
        xtype: 'widgetcolumn',
        width : 50,
        widget: {
            xtype: 'button',
            width: 30,
            text: 'A',
            tooltip: 'Assign Trade (Option)',
            handler: 'onAssignTrade'
        },
        onWidgetAttach: function(col, widget, rec) {
            widget.show();
            if ("OPEN" != rec.data.status || "OPT" != rec.data.secType) {
                widget.hide();
            }
        }
    }],
    dockedItems: [{
        xtype: 'pagingtoolbar',
        reference: 'tradesPaging',
        bind: '{trades}',
        dock: 'bottom',
        displayInfo: true
    }]
});