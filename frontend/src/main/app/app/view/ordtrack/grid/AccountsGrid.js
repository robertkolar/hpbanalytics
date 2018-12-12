/**
 * Created by robertk on 4/18/2015.
 */
Ext.define('HanGui.view.ordtrack.grid.AccountsGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.form.field.Checkbox',
        'Ext.form.field.Number',
        'Ext.form.field.Text',
        'Ext.grid.column.Action',
        'Ext.grid.column.Check',
        'Ext.grid.plugin.RowEditing',
        'HanGui.view.ordtrack.OrdTrackController'
    ],
    xtype: 'han-ordtrack-accounts-grid',
    listeners: {
        select: 'onAccountSelect'
    },
    bind: '{ibAccounts}',
    title: 'IB Accounts',
    viewConfig: {
        stripeRows: true
    },
    columns: [{
        text: 'Account ID',
        width: 120,
        dataIndex: 'accountId'
    }, {
        text: 'Connect',
        xtype: 'actioncolumn',
        width: 140,
        align: 'center',
        items: [{
            icon: 'resources/images/play-circle.png',
            tooltip: 'Connect',
            handler: 'connectIb'
        }, {
            icon: 'resources/images/pause.png',
            tooltip: 'Disconnect',
            handler: 'disconnectIb'
        }]
    }, {
        text: 'Status',
        width: 80,
        align: 'center',
        dataIndex: 'connected',
        renderer: 'connectStatusRenderer'
    }, {
        text: 'Host',
        width: 150,
        dataIndex: 'host',
        editor: {
            xtype: 'textfield',
            allowBlank: false
        }
    }, {
        text: 'Port',
        width: 80,
        dataIndex: 'port',
        align: 'right',
        editor: {
            xtype: 'numberfield',
            minValue: 1,
            maxValue: 65535,
            allowDecimals: false
        }
    }, {
        text: 'Cli Id',
        width: 80,
        dataIndex: 'clientId',
        align: 'right',
        editor: {
            xtype: 'numberfield',
            minValue: 0,
            maxValue: 65535,
            allowDecimals: false
        }
    }, {
        flex: 1
    }, {
        text: 'Lst',
        width: 60,
        dataIndex: 'listen',
        xtype: 'checkcolumn',
        editor: {
            xtype: 'checkboxfield'
        }
    }, {
        text: 'Upd',
        width: 60,
        dataIndex: 'allowUpd',
        xtype: 'checkcolumn',
        editor: {
            xtype: 'checkboxfield'
        }
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
    }],
    dockedItems: [{
        xtype: 'pagingtoolbar',
        bind: '{ibAccounts}',
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