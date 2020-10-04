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
        text: 'Fill Date',
        width: 180,
        dataIndex: 'fillDate',
        xtype: 'datecolumn',
        format: 'm/d/Y H:i:s'
    }, {
        text: 'Origin',
        width: 100,
        dataIndex: 'origin'
    }, {
        text: 'RefID',
        width: 100,
        dataIndex: 'referenceId',
        align: 'right'
    }, {
        text: 'Action',
        width: 60,
        dataIndex: 'action',
        renderer: function(val, metadata, record) {
            metadata.style = (val == 'BUY' ? 'color: blue;' : 'color: brown;');
            return val;
        }
    }, {
        text: 'Qnt',
        width: 80,
        dataIndex: 'quantity',
        align: 'right'
    }, {
        text: 'Sec',
        width: 60,
        dataIndex: 'secType',
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
        text: 'Symbol',
        width: 180,
        dataIndex: 'symbol',
        filter: 'string'
    }, {
        text: 'Fill',
        width: 100,
        dataIndex: 'fillPrice',
        align: 'right',
        renderer: function(val, metadata, record) {
            return Ext.util.Format.number(val, '0.00###');
        }
    }, {
        text: 'Received Date',
        width: 180,
        dataIndex: 'receivedDate',
        xtype: 'datecolumn',
        format: 'm/d/Y H:i:s'
    }, {
        text: 'Comment',
        flex: 1,
        dataIndex: 'comment',
        renderer: function(val, metadata, record) {
            return (val ? val.toLowerCase() : val);
        }
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