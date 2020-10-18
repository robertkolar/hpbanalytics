/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.view.statistics.StatisticsGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'han-statistics-grid',
    requires: [
        'Ext.grid.column.Date',
        'Ext.toolbar.Paging',
        'HanGui.view.statistics.StatisticsController',
        'Ext.form.field.ComboBox',
        'Ext.form.field.Checkbox'
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
        xtype: 'datecolumn',
        format: 'm/d/Y'
    }, {
        text: '#Execs',
        width: 80,
        dataIndex: 'numExecs',
        align: 'right'
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
        format: '0.00%',
        text: 'Win%',
        width: 100,
        dataIndex: 'pctWinners',
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
                    {"abbr": "DAYS", "name": "Daily"},
                    {"abbr": "MONTHS", "name": "Monthly"},
                    {"abbr": "YEARS", "name": "Yearly"}
                ]
            }),
            value: 'MONTHS',
            listeners: {
                change: 'reloadStatisticsAndCharts'
            }
        }, {
            xtype: 'combobox',
            editable: false,
            queryMode: 'local',
            displayField: 'name',
            valueField: 'abbr',
            reference: 'tradeTypeCombo',
            fieldLabel: 'TradeType',
            width: 150,
            labelWidth: 65,
            store: Ext.create('Ext.data.Store', {
                fields: ['abbr', 'name'],
                data: [
                    {"abbr": "ALL", "name": "--All--"},
                    {"abbr": "LONG", "name": "Long"},
                    {"abbr": "SHORT", "name": "Short"}
                ]
            }),
            value: 'ALL',
            margin: '0 0 0 10',
            listeners: {
                change: 'reloadStatisticsAndCharts'
            }
        }, {
            xtype: 'combobox',
            editable: false,
            queryMode: 'local',
            displayField: 'name',
            valueField: 'abbr',
            reference: 'secTypeCombo',
            fieldLabel: 'SecType',
            width: 150,
            labelWidth: 60,
            store: Ext.create('Ext.data.Store', {
                fields: ['abbr', 'name'],
                data: [
                    {"abbr": "ALL", "name": "--All--"},
                    {"abbr": "OPT", "name": "OPT"},
                    {"abbr": "FOP", "name": "FOP"},
                    {"abbr": "FUT", "name": "FUT"},
                    {"abbr": "CFD", "name": "CFD"},
                    {"abbr": "STK", "name": "STK"}
                ]
            }),
            value: 'ALL',
            margin: '0 0 0 10',
            listeners: {
                change: 'reloadStatisticsAndCharts'
            }
        }, {
            xtype: 'combobox',
            editable: false,
            queryMode: 'local',
            displayField: 'name',
            valueField: 'abbr',
            reference: 'currencyCombo',
            fieldLabel: 'Currency',
            width: 150,
            labelWidth: 60,
            store: Ext.create('Ext.data.Store', {
                fields: ['abbr', 'name'],
                data: [
                    {"abbr": "ALL", "name": "--All--"},
                    {"abbr": "USD", "name": "USD"},
                    {"abbr": "EUR", "name": "EUR"},
                    {"abbr": "CHF", "name": "CHF"},
                    {"abbr": "GBP", "name": "GBP"},
                    {"abbr": "JPY", "name": "JPY"},
                    {"abbr": "AUD", "name": "AUD"},
                    {"abbr": "KRW", "name": "KRW"},
                    {"abbr": "HKD", "name": "HKD"},
                    {"abbr": "SGD", "name": "SGD"}
                ]
            }),
            value: 'ALL',
            margin: '0 0 0 10',
            listeners: {
                change: 'reloadStatisticsAndCharts'
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
                fields: ['abbr', 'name'],
                data: [
                    {"abbr": "ALL", "name": "--All--"}
                ]
            }),
            value: 'ALL',
            margin: '0 0 0 10',
            listeners: {
                change: 'reloadStatisticsAndCharts'
            }
        }, {
            xtype: 'checkbox',
            reference: 'openOnlyCheckbox',
            fieldLabel: 'O',
            width: 30,
            labelWidth: 10,
            checked : false,
            margin: '0 0 0 10',
            listeners: {
                change: 'prepareUnderlyingCombo'
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
            margin: '0 0 0 50',
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
                data: [{'year': 2016}]
            })
        }, {
            xtype: 'combobox',
            margin: '0 0 0 10',
            editable: false,
            queryMode: 'local',
            displayField: 'endMonth',
            valueField: 'endMonth',
            reference: 'ifiEndMonthCombo',
            fieldLabel: '',
            width: 50,
            labelWidth: 0,
            store: Ext.create('Ext.data.Store', {
                fields: ['endMonth'],
                data: [{'endMonth': 1}]
            })
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
                    {'abbr': 'LONG', 'name': 'Long'},
                    {'abbr': 'SHORT', 'name': 'Short'}
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
