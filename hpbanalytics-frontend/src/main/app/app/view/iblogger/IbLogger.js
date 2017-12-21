/**
 * Created by robertk on 4/17/15.
 */
Ext.define('HanGui.view.iblogger.IbLogger', {
    extend: 'Ext.panel.Panel',
    xtype: 'han-iblogger',
    reference: 'iblogger',
    header: false,
    border: false,
    requires: [
        'Ext.layout.container.VBox',
        'HanGui.view.iblogger.grid.AccountsGrid',
        'HanGui.view.iblogger.IbLoggerController',
        'HanGui.view.iblogger.IbLoggerModel',
        'HanGui.view.iblogger.grid.OrdersGrid'
    ],
    controller: 'han-iblogger',
    viewModel: {
        type: 'han-iblogger'
    },
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    scrollable: true,
    items: [{
        xtype: 'han-iblogger-accounts-grid',
        reference: 'accountsGrid'
    }, {
        xtype: 'han-iblogger-orders-grid',
        reference: 'ordersGrid'
    }]
});