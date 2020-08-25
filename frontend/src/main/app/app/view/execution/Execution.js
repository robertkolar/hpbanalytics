/**
 * Created by robertk on 8/24/2020.
 */
Ext.define('HanGui.view.execution.Execution', {
    extend: 'Ext.panel.Panel',

    requires: [
        'Ext.layout.container.VBox',
        'HanGui.view.execution.ExecutionController',
        'HanGui.view.execution.ExecutionModel',
        'HanGui.view.execution.ExecutionGrid'
    ],

    xtype: 'han-execution',
    header: false,
    border: false,
    controller: 'han-execution',
    viewModel: {
        type: 'han-execution'
    },
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    scrollable: true,
    items: [{
        xtype: 'han-execution-grid',
        reference: 'executionGrid'
    }]
});
