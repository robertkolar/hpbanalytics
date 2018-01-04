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
        'HanGui.view.ordtrack.OrdTrack',
        'HanGui.view.report.Report',
        'HanGui.model.ordtrack.IbOrder',
        'HanGui.model.ordtrack.IbOrderEvent',
        'HanGui.model.report.Trade',
        'HanGui.model.report.SplitExecution'
    ],
    
    controller: 'main',
    viewModel: {
        type: 'main'
    },
    listeners: {
        beforerender: 'setGlyphs'
    },
    items: [{
        xtype: 'han-ordtrack',
        title: 'Ord Track',
        reference: 'ordTrackPanel'
    }, {
        xtype: 'han-report',
        title: 'Report',
        reference: 'reportPanel'
    }]
});
