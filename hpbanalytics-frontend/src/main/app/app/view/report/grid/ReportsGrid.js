/**
 * Created by robertk on 9/6/15.
 */
Ext.define('HanGui.view.report.grid.ReportsGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'han-report-reports-grid',
    requires: [
        'Ext.grid.column.Date',
        'Ext.toolbar.Paging',
        'Ext.grid.plugin.RowEditing',
        'HanGui.view.report.ReportController'
    ],
    listeners: {
        select: 'onReportSelect'
    },
    bind: '{reports}',
    viewConfig: {
        stripeRows: true
    },
    columns: [{
        text: 'ID',
        width: 60,
        dataIndex: 'id'
    }, {
        text: 'Name',
        width: 120,
        dataIndex: 'reportName',
        editor: {
            xtype: 'textfield',
            allowBlank: false
        }
    }, {
        text: 'Origin',
        width: 250,
        dataIndex: 'origin',
        editor: {
            xtype: 'textfield',
            allowBlank: false
        }
    }, {
        text: 'Exec',
        width: 120,
        dataIndex: 'numExecutions',
        align: 'right'
    }, {
        xtype: 'templatecolumn',
        text: 'Trds-Open',
        width: 120,
        tpl: '{numTrades}-{numOpenTrades}',
        align: 'right'
    }, {
        xtype: 'templatecolumn',
        text: 'Undl-Open',
        width: 120,
        tpl: '{numUnderlyings}-{numOpenUnderlyings}',
        align: 'right'
    }, {
        text: 'Started',
        width: 180,
        dataIndex: 'firstExecutionDate',
        xtype: 'datecolumn',
        format: 'm/d/Y H:i:s.u'
    }, {
        text: 'Last',
        flex: 1,
        dataIndex: 'lastExecutionDate',
        xtype: 'datecolumn',
        format: 'm/d/Y H:i:s.u'
    }, {
        text: 'Stk',
        width: 60,
        dataIndex: 'stk',
        xtype: 'checkcolumn',
        editor: {
            xtype: 'checkboxfield'
        }
    }, {
        text: 'Opt',
        width: 60,
        dataIndex: 'opt',
        xtype: 'checkcolumn',
        editor: {
            xtype: 'checkboxfield'
        }
    }, {
        text: 'Fut',
        width: 60,
        dataIndex: 'fut',
        xtype: 'checkcolumn',
        editor: {
            xtype: 'checkboxfield'
        }
    }, {
        text: 'Fx',
        width: 60,
        dataIndex: 'fx',
        xtype: 'checkcolumn',
        editor: {
            xtype: 'checkboxfield'
        }
    }, {
        text: 'Cfd',
        width: 60,
        dataIndex: 'cfd',
        xtype: 'checkcolumn',
        editor: {
            xtype: 'checkboxfield'
        }
    }, {
        xtype: 'widgetcolumn',
        width : 50,
        widget: {
            xtype: 'button',
            width: 30,
            tooltip: 'Analyze Report',
            handler: 'onAnalyzeReport',
            listeners: {
                beforerender: function(c, eOpts) {
                    c.setGlyph(HanGui.common.Glyphs.getGlyph('gear'));
                }
            }
        }
    }, {
        xtype: 'widgetcolumn',
        width : 50,
        widget: {
            xtype: 'button',
            width: 30,
            tooltip: 'Delete Report',
            handler: 'onDeleteReport',
            listeners: {
                beforerender: function(c, eOpts) {
                    c.setGlyph(HanGui.common.Glyphs.getGlyph('delete'));
                }
            }
        }
    }],
    dockedItems: [{
        xtype: 'pagingtoolbar',
        bind: '{reports}',
        dock: 'bottom',
        displayInfo: true
    }],
    plugins: {
        ptype: 'rowediting',
        clicksToEdit: 2,
        listeners: {
            edit: function (editor, ctx, eOpts) {ctx.grid.getStore().sync()}
        }
    }
});