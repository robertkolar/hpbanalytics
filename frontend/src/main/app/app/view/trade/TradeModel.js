/**
 * Created by robertk on 8/24/2020.
 */
Ext.define('HanGui.view.trade.TradeModel', {
    extend: 'Ext.app.ViewModel',
    requires: [
        'HanGui.model.Trade'
    ],

    alias: 'viewmodel.han-trade',

    stores: {
        trades: {
            model: 'HanGui.model.Trade',
            pageSize: 25,
            remoteFilter: true,
            remoteSort: false
        }
    }
});
