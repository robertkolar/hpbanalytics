/**
 * Created by robertk on 8/24/2020.
 */
Ext.define('HanGui.view.statistics.Statistics', {
    extend: 'Ext.panel.Panel',

    requires: [
        'Ext.layout.container.VBox',
        'HanGui.view.statistics.StatisticsController',
        'HanGui.view.statistics.StatisticsModel',
        'HanGui.view.statistics.StatisticsGrid'
    ],

    xtype: 'han-statistics',
    header: false,
    border: false,
    controller: 'han-statistics',

    viewModel: {
        type: 'han-statistics'
    },
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    scrollable: true,
    items: [{
        xtype: 'han-statistics-grid',
        reference: 'statisticsGrid'
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
            html: '<div id="hpb_c6"></div>'
        }, {
            html: '<div id="hpb_c7"></div>'
        }, {
            html: '<div id="hpb_c8"></div>',
            margin: '0 0 50 0'
        }]
    }]
});
