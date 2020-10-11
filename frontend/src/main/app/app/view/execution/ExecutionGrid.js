/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.view.execution.ExecutionGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'han-execution-grid',
    requires: [
        'Ext.grid.column.Date',
        'Ext.toolbar.Paging',
        'HanGui.view.execution.ExecutionController',
        'Ext.grid.filters.Filters'
    ],
    plugins: 'gridfilters',
    bind: '{executions}',
    viewConfig: {
        stripeRows: true
    },
    columns: [{
        text: 'ID',
        width: 60,
        dataIndex: 'id'
    }, {
        text: 'Received Date',
        width: 160,
        dataIndex: 'receivedDate',
        xtype: 'datecolumn',
        format: 'm/d/Y H:i:s'
    }, {
        text: 'Reference',
        width: 180,
        dataIndex: 'reference',
        align: 'right'
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
        text: 'Mul',
        width: 60,
        dataIndex: 'multiplier',
        align: 'right'
    }, {
        text: 'Fill Date',
        width: 160,
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
    }, {
        flex: 1,
        menuDisabled: true
    }, {
        xtype: 'widgetcolumn',
        width : 50,
        widget: {
            xtype: 'button',
            width: 30,
            tooltip: 'Delete Execution',
            handler: 'onDeleteExecution',
            listeners: {
                beforerender: function(c, eOpts) {
                    c.setGlyph(HanGui.common.Glyphs.getGlyph('delete'));
                }
            }
        }
    }],
    dockedItems: [{
        xtype: 'pagingtoolbar',
        reference: 'executionPaging',
        bind: '{executions}',
        dock: 'bottom',
        displayInfo: true
    }, {
        xtype: 'toolbar',
        items: [{
            xtype: 'button',
            margin: '0 0 0 10',
            text: 'Add',
            handler: 'onAddExecution',
            listeners: {
                beforerender: function(c, eOpts) {
                    c.setGlyph(HanGui.common.Glyphs.getGlyph('add'));
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
