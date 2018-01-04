/**
 * Created by robertk on 4/17/2015.
 */
Ext.define('HanGui.view.ordtrack.OrdTrack', {
    extend: 'Ext.panel.Panel',
    xtype: 'han-ordtrack',
    reference: 'ordtrack',
    header: false,
    border: false,
    requires: [
        'Ext.layout.container.VBox',
        'HanGui.view.ordtrack.grid.AccountsGrid',
        'HanGui.view.ordtrack.OrdTrackController',
        'HanGui.view.ordtrack.OrdTrackModel',
        'HanGui.view.ordtrack.grid.OrdersGrid',
        'HanGui.view.ordtrack.grid.PositionsGrid',
        'HanGui.common.Glyphs',
        'Ext.tab.Panel'

    ],
    controller: 'han-ordtrack',
    viewModel: {
        type: 'han-ordtrack'
    },
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    scrollable: true,
    items: [{
        xtype: 'han-ordtrack-accounts-grid',
        reference: 'accountsGrid'
    }, {
        xtype: 'tabpanel',
        title: 'Details',
        listeners: {
            beforerender: 'setGlyphs'
        },
        items: [{
            xtype: 'han-ordtrack-orders-grid',
            title: 'IB Orders',
            reference: 'ordersPanel'
        }, {
            xtype: 'han-ordtrack-positions-grid',
            title: 'Positions',
            reference: 'positionsPanel'
        }]
    }]
});