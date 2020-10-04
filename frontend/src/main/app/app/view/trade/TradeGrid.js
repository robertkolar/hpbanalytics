/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.view.trade.TradeGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'han-trade-grid',
    requires: [
        'Ext.grid.column.Date',
        'Ext.toolbar.Paging',
        'HanGui.view.trade.TradeController',
        'Ext.grid.filters.Filters'
    ],
    plugins: 'gridfilters',
    controller: 'han-trade',
    bind: '{trades}',
    viewConfig: {
        stripeRows: true
    },
    listeners: {
        'cellclick': 'showSplitExecutions'
    },
    columns: [{
        text: 'ID',
        width: 60,
        dataIndex: 'id'
    }, {
        text: 'Open Date',
        width: 180,
        dataIndex: 'openDate',
        xtype: 'datecolumn',
        format: 'm/d/Y H:i:s'
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
        filter: 'string'
    }, {
        text: 'Undl',
        width: 80,
        dataIndex: 'underlying'
    }, {
        text: 'Symbol',
        width: 180,
        dataIndex: 'symbol',
        filter: 'string'
    }, {
        text: 'Cur',
        width: 80,
        dataIndex: 'currency'
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
        format: 'm/d/Y H:i:s'
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
        filter: 'string'
    }, {
        xtype: 'widgetcolumn',
        width : 50,
        widget: {
            xtype: 'button',
            width: 30,
            tooltip: 'Close Trade',
            handler: 'onCloseTrade',
            listeners: {
                beforerender: function(c, eOpts) {
                    c.setGlyph(HanGui.common.Glyphs.getGlyph('times'));
                }
            }
        },
        onWidgetAttach: function(col, widget, rec) {
            widget.show();
            if ("OPEN" != rec.data.status) {
                widget.hide();
            }
        }
    }],
    dockedItems: [{
        xtype: 'pagingtoolbar',
        reference: 'tradePaging',
        bind: '{trades}',
        dock: 'bottom',
        displayInfo: true
    }, {
        xtype: 'toolbar',
        items: [{
            xtype: 'tbtext',
            flex: 1
        }, {
            xtype: 'tbtext',
            html: 'WS status',
            width: 120,
            margin: '0 0 0 10',
            reference: 'wsStatus'
        }]
    }]
});
