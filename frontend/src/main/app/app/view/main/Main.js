/**
 * This class is the main view for the application. It is specified in app.js as the
 * "autoCreateViewport" property. That setting automatically applies the "viewport"
 * plugin to promote that instance of this class to the body element.
 *
 */
Ext.define('HanGui.view.main.Main', {
    extend: 'Ext.tab.Panel',

    requires: [
        'HanGui.view.main.MainController',
        'HanGui.view.main.MainModel',
        'HanGui.view.execution.Execution',
        'HanGui.view.trade.Trade',
        'HanGui.view.statistics.Statistics'
    ],
    
    controller: 'main',
    viewModel: {
        type: 'main'
    },
    listeners: {
        beforerender: 'setGlyphs'
    },
    items: [{
        xtype: 'han-execution',
        title: 'Executions'
    }, {
        xtype: 'han-trade',
        title: 'Trades'
    }, {
        xtype: 'han-statistics',
        title: 'Statistics'
    }]
});
