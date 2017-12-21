/**
 * Created by robertk on 9/6/15.
 */
Ext.define('HanGui.view.report.Report', {
    extend: 'Ext.panel.Panel',

    requires: [
        'Ext.layout.container.VBox',
        'HanGui.view.report.ReportController',
        'HanGui.view.report.ReportModel',
        'HanGui.view.report.grid.ReportsGrid',
        'HanGui.view.report.grid.ExecutionsGrid',
        'HanGui.view.report.grid.TradesGrid',
        'HanGui.view.report.grid.StatisticsGrid',
        'HanGui.common.Glyphs',
        'Ext.tab.Panel',
        'HanGui.view.report.window.ExecutionAddWindow'
    ],

    xtype: 'han-report',
    header: false,
    border: false,
    controller: 'han-report',
    viewModel: {
        type: 'han-report'
    },
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    scrollable: true,
    items: [{
        xtype: 'han-report-reports-grid',
        title: 'Reports',
        reference: 'reportsGrid'
    }, {
        xtype: 'tabpanel',
        title: 'Details',
        listeners: {
            beforerender: 'setGlyphs'
        },
        items: [{
            xtype: 'han-report-executions-grid',
            title: 'Executions',
            reference: 'executionsPanel'
        }, {
            xtype: 'han-report-trades-grid',
            title: 'Trades',
            reference: 'tradesPanel'
        }, {
            xtype: 'panel',
            title: 'Statistics',
            reference: 'statisticsPanel',
            items: [{
                xtype: 'han-report-statistics-grid'
            }, {
                xtype: 'container',
                reference: 'chartsContainer',
                defaults: {
                    width: 1500,
                    height: 200
                },
                items: [{
                    html: '<div id="hpb_c1" style="height: 100%"></div>',
                    height: 400
                }, {
                    html: '<div id="hpb_c2" style="height: 100%"></div>',
                    height: 400
                }, {
                    html: '<div id="hpb_c3"></div>'
                }, {
                    html: '<div id="hpb_c4"></div>'
                }, {
                    html: '<div id="hpb_c5"></div>'
                }, {
                    html: '<div id="hpb_c6"></div>',
                    margin: '0 0 50 0'
                }]
            }]
        }]
    }]
});