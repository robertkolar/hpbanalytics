/**
 * Created by robertk on 4/17/2015.
 */
Ext.define('HanGui.view.ordtrack.OrdTrackModel', {
    extend: 'Ext.app.ViewModel',
    requires: [
        'HanGui.model.ordtrack.IbOrder',
        'HanGui.model.ordtrack.Position',
        'HanGui.model.ordtrack.IbAccount'
    ],

    alias: 'viewmodel.han-ordtrack',

    stores: {
        ibOrders: {
            model: 'HanGui.model.ordtrack.IbOrder',
            autoload: true,
            pageSize: 25,
            remoteFilter: true,
            remoteSort: false
        },
        positions: {
            model: 'HanGui.model.ordtrack.Position',
            autoload: true,
            pageSize: 25
        },
        ibAccounts: {
            model: 'HanGui.model.ordtrack.IbAccount',
            autoload: true,
            pageSize: 10
        }
    }
});