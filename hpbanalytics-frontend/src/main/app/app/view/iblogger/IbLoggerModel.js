/**
 * Created by robertk on 4/17/15.
 */
Ext.define('HanGui.view.iblogger.IbLoggerModel', {
    extend: 'Ext.app.ViewModel',
    requires: [
        'HanGui.model.iblogger.IbOrder',
        'HanGui.model.iblogger.IbAccount'
    ],

    alias: 'viewmodel.han-iblogger',

    stores: {
        ibOrders: {
            model: 'HanGui.model.iblogger.IbOrder',
            autoload: true,
            pageSize: 25,
            remoteFilter: true,
            remoteSort: false
        },
        ibAccounts: {
            model: 'HanGui.model.iblogger.IbAccount',
            autoload: true,
            pageSize: 10
        }
    }
});