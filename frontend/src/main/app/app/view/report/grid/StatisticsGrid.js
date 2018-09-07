/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.view.report.grid.StatisticsGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'han-report-statistics-grid',
    requires: [
        'Ext.grid.column.Date',
        'Ext.toolbar.Paging',
        'HanGui.view.report.ReportController',
        'Ext.form.field.ComboBox'
    ],
    bind: '{statistics}',
    viewConfig: {
        stripeRows: true
    },
    columns: [{
        text: '#',
        width: 60,
        dataIndex: 'id'
    }, {
        text: 'Period',
        width: 100,
        dataIndex: 'periodDate',
        renderer: function(val, metadata, record) {
            return Ext.util.Format.date(new Date(val), 'm/d/Y');
        }
    }, {
        text: '#Opn',
        width: 80,
        dataIndex: 'numOpened',
        align: 'right'
    }, {
        text: '#Cls',
        width: 80,
        dataIndex: 'numClosed',
        align: 'right'
    }, {
        text: '#Win',
        width: 80,
        dataIndex: 'numWinners',
        align: 'right'
    }, {
        text: '#Los',
        width: 80,
        dataIndex: 'numLosers',
        align: 'right'
    }, {
        xtype: 'numbercolumn',
        format: '0.00',
        text: 'Big W',
        width: 100,
        dataIndex: 'bigWinner',
        align: 'right'
    }, {
        xtype: 'numbercolumn',
        format: '0.00',
        text: 'Big L',
        width: 100,
        dataIndex: 'bigLoser',
        align: 'right'
    }, {
        xtype: 'numbercolumn',
        format: '0.00',
        text: 'W Profit',
        width: 100,
        dataIndex: 'winnersProfit',
        align: 'right'
    }, {
        xtype: 'numbercolumn',
        format: '0.00',
        text: 'L Loss',
        width: 100,
        dataIndex: 'losersLoss',
        align: 'right'
    }, {
        text: 'PL Period',
        width: 100,
        dataIndex: 'profitLoss',
        align: 'right',
        renderer: function(val, metadata, record) {
            metadata.style = val < 0 ? 'color: red;' : 'color: green;';
            return Ext.util.Format.number(val, '0.00');
        }
    }, {
        text: 'Cumul PL',
        width: 100,
        dataIndex: 'cumulProfitLoss',
        align: 'right',
        renderer: function(val, metadata, record) {
            metadata.style = val < 0 ? 'color: red;' : 'color: green;';
            return Ext.util.Format.number(val, '0.00');
        }
    }, {
        flex: 1
    }],
    dockedItems: [{
        xtype: 'pagingtoolbar',
        reference: 'statisticsPaging',
        bind: '{statistics}',
        dock: 'bottom',
        displayInfo: true
    }, {
        xtype: 'toolbar',
        items: [{
            xtype: 'combobox',
            editable: false,
            queryMode: 'local',
            displayField: 'name',
            valueField: 'abbr',
            reference: 'intervalCombo',
            fieldLabel: 'Interval',
            width: 150,
            labelWidth: 50,
            store: Ext.create('Ext.data.Store', {
                fields: ['abbr', 'name'],
                data: [
                    {"abbr": "DAY", "name": "Daily"},
                    {"abbr": "MONTH", "name": "Monthly"}
                ]
            }),
            value: 'MONTH',
            listeners: {
                change: 'onIntervalChange'
            }
        }, {
            xtype: 'combobox',
            editable: false,
            queryMode: 'local',
            displayField: 'name',
            valueField: 'abbr',
            reference: 'underlyingCombo',
            fieldLabel: 'Underlying',
            width: 170,
            labelWidth: 70,
            store: Ext.create('Ext.data.ArrayStore', {
                fields: ['abbr', 'name']
            }),
            margin: '0 0 0 10',
            listeners: {
                change: 'onUnderlyingChange'
            }
        }, {
            xtype: 'button',
            margin: '0 0 0 10',
            text: 'Calculate',
            handler: 'onCalculateStatistics',
            listeners: {
                beforerender: function(c, eOpts) {
                    c.setGlyph(HanGui.common.Glyphs.getGlyph('gear'));
                }
            }
        }, {
            xtype: 'button',
            reference: 'chartsButton',
            enableToggle: true,
            margin: '0 0 0 10',
            text: 'Charts',
            listeners: {
                beforerender: function(c, eOpts) {
                    c.setGlyph(HanGui.common.Glyphs.getGlyph('barchart'));
                },
                toggle: 'onChartsToggle'
            }
        }, {
            xtype: 'combobox',
            margin: '0 0 0 20',
            editable: false,
            queryMode: 'local',
            displayField: 'year',
            valueField: 'year',
            reference: 'ifiYearCombo',
            fieldLabel: 'IFI Report',
            width: 140,
            labelWidth: 65,
            store: Ext.create('Ext.data.Store', {
                fields: ['year'],
                data: [{"year": "2016"}, {"year": "2017"}, {"year": "2018"}, {"year": "2019"}]
            }),
            value: '2018'
        }, {
            xtype: 'combobox',
            margin: '0 0 0 10',
            editable: false,
            queryMode: 'local',
            displayField: 'name',
            valueField: 'abbr',
            reference: 'ifiTradeTypeCombo',
            fieldLabel: 'Type',
            width: 110,
            labelWidth: 35,
            store: Ext.create('Ext.data.Store', {
                fields: ['abbr', 'name'],
                data: [
                    {"abbr": "LONG", "name": "Long"},
                    {"abbr": "SHORT", "name": "Short"}
                ]
            }),
            value: 'SHORT'
        }, {
            xtype: 'button',
            margin: '0 0 0 10',
            text: 'Generate',
            handler: 'onDownloadIfiReport',
            listeners: {
                beforerender: function(c, eOpts) {
                    c.setGlyph(HanGui.common.Glyphs.getGlyph('download'));
                }
            }
        }]
    }]
});