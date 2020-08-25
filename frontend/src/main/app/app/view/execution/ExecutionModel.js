/**
 * Created by robertk on 8/24/2020.
 */
Ext.define('HanGui.view.execution.ExecutionModel', {
    extend: 'Ext.app.ViewModel',
    requires: [
        'HanGui.model.Execution'
    ],

    alias: 'viewmodel.han-execution',

    stores: {
        executions: {
            model: 'HanGui.model.Execution',
            pageSize: 25,
            remoteFilter: true,
            remoteSort: false
        }
    }
});
