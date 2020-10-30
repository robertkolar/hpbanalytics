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
    bind: '{trades}',
    viewConfig: {
        stripeRows: true
    },
    listeners: {
        'cellclick': 'showTradeExecutions'
    },
    columns: [{
        text: 'ID',
        width: 80,
        dataIndex: 'id'
    }, {
        text: 'Type',
        width: 80,
        dataIndex: 'type',
        renderer: 'tradeTypeRenderer'
    }, {
        text: 'Undl',
        width: 80,
        dataIndex: 'underlying',
        filter: 'string'
    }, {
        text: 'Symbol',
        width: 180,
        dataIndex: 'symbol',
        filter: 'string'
    }, {
        text: 'Sec',
        width: 80,
        dataIndex: 'secType',
        filter: {
            type: 'list',
            options: HanGui.common.Definitions.secTypes
        }
    }, {
        text: 'Cur',
        width: 80,
        dataIndex: 'currency',
        filter: {
            type: 'list',
            options: HanGui.common.Definitions.currencies
        }
    }, {
        text: 'Mul',
        width: 60,
        dataIndex: 'multiplier',
        align: 'right',
        filter: 'number'
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
        text: 'Open',
        width: 100,
        dataIndex: 'avgOpenPrice',
        align: 'right',
        renderer: 'priceRenderer'
    }, {
        text: 'Open Date',
        width: 160,
        dataIndex: 'openDate',
        xtype: 'datecolumn',
        format: 'm/d/Y H:i:s'
    }, {
        text: 'Close',
        width: 100,
        dataIndex: 'avgClosePrice',
        align: 'right',
        renderer: 'priceRenderer'
    }, {
        text: 'Close Date',
        width: 160,
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
        renderer: 'profitLossRenderer'
    }, {
        text: 'Execution IDs',
        flex: 1,
        dataIndex: 'executionIds',
        renderer: 'pointerRenderer'
    }, {
        text: 'Status',
        width: 60,
        dataIndex: 'status',
        renderer: 'tradeStatusRenderer',
        filter: {
            type: 'list',
            options: ['OPEN', 'CLOSED']
        }
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
            if ("OPEN" !== rec.data.status) {
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
            xtype: 'button',
            margin: '0 0 0 10',
            text: 'Regenerate',
            handler: 'onRegenerateAllTrades',
            listeners: {
                beforerender: function(c, eOpts) {
                    c.setGlyph(HanGui.common.Glyphs.getGlyph('gear'));
                }
            }
        }, {
            xtype: 'button',
            margin: '0 0 0 20',
            text: '',
            handler: 'refreshTradeStatistics',
            listeners: {
                beforerender: function(c, eOpts) {
                    c.setGlyph(HanGui.common.Glyphs.getGlyph('refresh'));
                }
            }
        }, {
            xtype: 'tbtext',
            width: 600,
            margin: '0 0 0 10',
            reference: 'tradeStatistics'
        }, {
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
