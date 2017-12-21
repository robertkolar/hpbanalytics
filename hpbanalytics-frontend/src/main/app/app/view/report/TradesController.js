/**
 * Created by robertk on 15.10.2015.
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

        var closeDate = new Date(),
            maxCloseDate,
            closePrice = trade.avgOpenPrice,
            ready = false;

        me.getView().setLoading({border: false, msg: 'Loading...'});
        if ('OPT' != trade.secType) {
            ready = true;
        } else {
            Ext.Ajax.request({
                method: 'GET',
                url: HanGui.common.Definitions.urlPrefixReport + '/optionutil/parse',
                params: {
                    optionsymbol: trade.symbol
                },
                success: function (response, opts) {
                    var expDate = JSON.parse(response.responseText).expDate;
                    maxCloseDate = new Date(expDate + 24 * 60 * 60 * 1000);
                    ready = true;
                }
            });
        }

        var interval = setInterval(function() {
            if (ready) {
                clearInterval(interval);
                var window = Ext.create('HanGui.view.report.window.TradeCloseWindow', {
                    reference: 'tradeCloseWindow',
                    title: 'Close Trade, id=' + trade.id
                });
                me.getView().add(window);
                window.trade = trade;
                me.lookupReference('closeDate').setValue(closeDate);
                if (maxCloseDate) {
                    me.lookupReference('closeDate').setMaxValue(maxCloseDate);
                }
                me.lookupReference('closePrice').setValue(closePrice);
                me.getView().setLoading(false);
                window.show();
            } else {
                console.log('Waiting for completion...');
            }
        }, 1000);
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

    onExpireTrade: function(button, evt) {
        var me = this,
            trade = button.getWidgetRecord().data;

        me.assignOrExpire('EXPIRE', trade);
    },

    onAssignTrade: function(button, evt) {
        var me = this,
            trade = button.getWidgetRecord().data;

        me.assignOrExpire('ASSIGN', trade);
    },

    assignOrExpire: function(requestType, trade, closeDate, closePrice) {
        var urlString = HanGui.common.Definitions.urlPrefixReport + '/reports/' + trade.reportId  + '/trades/' + trade.id + '/' + requestType.toLowerCase();

        Ext.Msg.show({
            title: requestType + ' Trade?',
            message: 'Are you sure you want to ' + requestType + ' the trade, id=' + trade.id + '?',
            buttons: Ext.Msg.YESNO,
            icon: Ext.Msg.QUESTION,
            fn: function(btn) {
                if (btn === 'yes') {
                    if ('CLOSE' == requestType) {
                        Ext.Ajax.request({
                            method: 'PUT',
                            jsonData: {
                                closeDate: closeDate,
                                closePrice: closePrice
                            },
                            url: urlString
                        });
                    } else {
                        Ext.Ajax.request({
                            method: 'PUT',
                            url: urlString
                        });
                    }
                }
            }
        });
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