/**
 * Created by robertk on 8/24/2020.
 */
Ext.define('HanGui.view.trade.Trade', {
    extend: 'Ext.panel.Panel',

    requires: [
        'Ext.layout.container.VBox',
        'HanGui.view.trade.TradeController',
        'HanGui.view.trade.TradeModel',
        'HanGui.view.trade.TradeGrid'
    ],

    xtype: 'han-trade',
    header: false,
    border: false,
    controller: 'han-trade',
    viewModel: {
        type: 'han-trade'
    },
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    scrollable: true,
    items: [{
        xtype: 'han-trade-grid',
        reference: 'tradeGrid'
    }]
});
