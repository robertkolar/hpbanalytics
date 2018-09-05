/**
 * Created by robertk on 10/15/2015.
 */
Ext.define('HanGui.view.report.TradesController', {
    extend: 'Ext.app.ViewController',

    requires: [
        'HanGui.common.Definitions',
        'HanGui.view.report.window.TradeCloseWindow',
        'HanGui.view.report.window.SplitExecutionsWindow'
    ],

    alias: 'controller.han-report-trades',

    onCloseTrade: function(button, evt) {
        var me = this,
            trade = button.getWidgetRecord().data;

        var window = Ext.create('HanGui.view.report.window.TradeCloseWindow', {
            reference: 'tradeCloseWindow',
            title: 'Close Trade, id=' + trade.id
        });
        me.getView().add(window);
        window.trade = trade;
        me.lookupReference('closeDate').setValue(new Date());
        me.lookupReference('closePrice').setValue(0.0);
        window.show();
    },

    onSubmitCloseTrade: function(button, evt) {
        var me = this,
            form = me.lookupReference('tradeCloseForm'),
            window = me.lookupReference('tradeCloseWindow'),
            trade = window.trade;

        var urlString = HanGui.common.Definitions.urlPrefixReport + '/reports/' + trade.reportId  + '/trades/' + trade.id + '/close';

        if (form && form.isValid()) {
            Ext.Ajax.request({
                method: 'PUT',
                url: urlString,
                jsonData: {
                    closeDate: new Date(form.getForm().findField('closeDate').lastValue).getTime(),
                    closePrice: form.getForm().findField('closePrice').lastValue
                },
                success: function (response, opts) {
                    window.close();
                }
            });
        }
    },

    onCancelCloseTrade: function(button, evt) {
        this.lookupReference('tradeCloseWindow').close();
    },

    showSplitExecutions: function (view, cell, cellIndex, record, row, rowIndex, e) {
        if (cellIndex != 15) {
            return;
        }
        var me = this;

        if (!me.splitExecutionsGrid) {
            me.splitExecutionsGrid =  Ext.create('HanGui.view.report.grid.SplitExecutionsGrid');
            me.splitExecutionsWindow = Ext.create('HanGui.view.report.window.SplitExecutionsWindow');
            me.splitExecutionsWindow.add(me.splitExecutionsGrid);
        }
        me.splitExecutionsGrid.setStore(record.splitExecutions());
        me.splitExecutionsWindow.setTitle("Split Executions for Trade id=" + record.data.id);
        me.splitExecutionsWindow.show();
    }
});