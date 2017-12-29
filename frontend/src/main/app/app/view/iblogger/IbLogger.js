/**
 * Created by robertk on 4/17/2015.
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
        'HanGui.view.iblogger.grid.OrdersGrid',
        'HanGui.view.iblogger.grid.PositionsGrid',
        'HanGui.common.Glyphs',
        'Ext.tab.Panel'

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
        xtype: 'tabpanel',
        title: 'Details',
        listeners: {
            beforerender: 'setGlyphs'
        },
        items: [{
            xtype: 'han-iblogger-orders-grid',
            title: 'IB Orders',
            reference: 'ordersPanel'
        }, {
            xtype: 'han-iblogger-positions-grid',
            title: 'Positions',
            reference: 'positionsPanel'
        }]
    }]
});