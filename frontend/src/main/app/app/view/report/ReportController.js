/**
 * Created by robertk on 9/6/15.
 */
Ext.define('HanGui.view.report.ReportController', {
    extend: 'Ext.app.ViewController',

    requires: [
        'HanGui.common.Definitions'
    ],

    alias: 'controller.han-report',

    init: function() {
        var me = this,
            reports = me.getStore('reports'),
            reportsGrid = me.lookupReference('reportsGrid');

        if (reports) {
            reports.getProxy().setUrl(HanGui.common.Definitions.urlPrefixReport + '/reports');
            reports.load(function(records, operation, success) {
                if (success) {
                    console.log('reloaded reports');
                    reportsGrid.setSelection(reports.first());
                }
            });
        }

        var socket  = new SockJS('/websocket');
        var stompClient = Stomp.over(socket);

        stompClient.connect({}, function(frame) {
            console.log("WS report connected");

            stompClient.subscribe('/topic/report', function(message) {
                me.reloadAll();
            });

        }, function() {
            console.log("WS report disconnected");
        });
    },

    reloadAll: function() {
        var me = this,
            reports = me.getStore('reports'),
            executions = me.getStore('executions'),
            trades = me.getStore('trades');

        me.lookupReference('chartsButton').toggle(false);
        reports.load(function(records, operation, success) {
            if (success) {
                console.log('reloaded reports');
            }
        });
        executions.reload();
        trades.reload();
        me.reloadStatisticsAndCharts();
    },

    onReportSelect: function(grid, record, index, eOpts) {
        var me = this,
            executions = me.getStore('executions'),
            trades = me.getStore('trades'),
            statistics = me.getStore('statistics'),
            charts = me.getStore('charts'),
            interval = me.lookupReference('intervalCombo').getValue(),
            underlyingCombo =  me.lookupReference('underlyingCombo'),
            executionsPaging = me.lookupReference('executionsPaging'),
            tradesPaging = me.lookupReference('tradesPanel').lookupReference('tradesPaging');

        me.reportId = record.data.id;

        executions.getProxy().setUrl(HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId  + '/executions');
        trades.getProxy().setUrl(HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId + '/trades');
        statistics.getProxy().setUrl(HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId + '/statistics/' + interval);
        charts.getProxy().setUrl(HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId + '/charts/' + interval);

        Ext.Ajax.request({
            url: HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId  + '/underlyings',
            success: function(response, opts) {
                var undls = Ext.decode(response.responseText);
                var undlsData = [];
                undlsData.push(['ALL', '--All--']);
                for (var i = 0; i < undls.length; i++) {
                    undlsData.push([undls[i], undls[i]]);
                }
                underlyingCombo.getStore().loadData(undlsData);
                underlyingCombo.setValue('ALL');
            }
        });

        me.lookupReference('chartsButton').toggle(false);
        if (executionsPaging.getStore().isLoaded()) {
            executionsPaging.moveFirst();
        } else {
            executions.load(function(records, operation, success) {
                if (success) {
                    console.log('reloaded executions for report, id=' + me.reportId)
                }
            });
        }
        if (tradesPaging.getStore().isLoaded()) {
            tradesPaging.moveFirst();
        } else {
            trades.load(function(records, operation, success) {
                if (success) {
                    console.log('reloaded trades for report, id=' + me.reportId)
                }
            });
        }
        me.reloadStatisticsAndCharts();
    },

    onIntervalChange: function(comboBox, newValue, oldValue, eOpts) {
        var me = this,
            interval = me.lookupReference('intervalCombo').getValue();

        me.getStore('statistics').getProxy().setUrl(HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId + '/statistics/' + interval);
        me.getStore('charts').getProxy().setUrl(HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId + '/charts/' + interval);
        me.reloadStatisticsAndCharts();
    },

    onCalculateStatistics: function(button, evt) {
        var me = this,
            interval = me.lookupReference('intervalCombo').getValue(),
            tradeType =  me.lookupReference('tradeTypeCombo').getValue(),
            secType =  me.lookupReference('secTypeCombo').getValue(),
            currency =  me.lookupReference('currencyCombo').getValue(),
            underlying =  me.lookupReference('underlyingCombo').getValue();

        Ext.Ajax.request({
            method: 'PUT',
            url: HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId  + '/statistics/' + interval,
            params: {tradeType: tradeType, secType: secType, currency: currency, underlying: underlying},

            success: function(response, opts) {
                me.reloadStatisticsAndCharts();
            }
        });
    },

    onDownloadIfiReport: function(button, evt) {
        var me = this,
            year = me.lookupReference('ifiYearCombo').getValue(),
            tradeType =  me.lookupReference('ifiTradeTypeCombo').getValue();

        Ext.Ajax.request({
            method: 'GET',
            url: HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId  + '/ificsv/' + year + '/' + tradeType,

            success: function(response, opts) {
                var content = response.responseText;
                var filename = 'IFI_' + year + '_' + tradeType + '.csv';

                console.log(content);
                var blob = new Blob([content], {
                    type: 'text/plain;charset=utf-8'
                });
                saveAs(blob, filename);
            }
        });
    },

    onAnalyzeReport: function(button) {
        var me = this;

        Ext.Msg.show({
            title:'Analyze Report?',
            message: 'All trades will be deleted and recreated again',
            buttons: Ext.Msg.YESNO,
            icon: Ext.Msg.QUESTION,
            fn: function(btn) {
                if (btn === 'yes') {
                    Ext.Ajax.request({
                        method: 'PUT',
                        url: HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId
                    });
                }
            }
        });
    },

    onDeleteReport: function(button) {
        var me = this,
            reports = me.getStore('reports'),
            reportsGrid = me.lookupReference('reportsGrid');

        Ext.Msg.show({
            title:'Delete Report?',
            message: 'All trades will be deleted',
            buttons: Ext.Msg.YESNO,
            icon: Ext.Msg.QUESTION,
            fn: function(btn) {
                if (btn === 'yes') {
                    Ext.Ajax.request({
                        method: 'DELETE',
                        url: HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId ,
                        success: function(response, opts) {
                            reports.load(function(records, operation, success) {
                                if (success) {
                                    reportsGrid.setSelection(reports.first());
                                }
                            });
                        }
                    });
                }
            }
        });
    },

    onAddExecution: function(button, e, options) {
        var me = this;

        me.lookupReference('executionsPanel').add(Ext.create('HanGui.view.report.window.ExecutionAddWindow', {
            reference: 'executionAddWindow',
            title: 'Add New Execution for Report id=' + me.reportId
        })).show();
    },

    onSubmitAddExecution: function(button, e, options) {
        var me = this,
            form = me.lookupReference('executionAddForm'),
            window = me.lookupReference('executionAddWindow');

        if (form && form.isValid()) {
            Ext.Ajax.request({
                method: 'POST',
                url: HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId + '/executions',
                jsonData: {
                    origin: form.getForm().findField('origin').lastValue,
                    referenceId: form.getForm().findField('referenceId').lastValue,
                    action: form.getForm().findField('action').lastValue,
                    quantity: form.getForm().findField('quantity').lastValue,
                    underlying: form.getForm().findField('underlying').lastValue,
                    currency: form.getForm().findField('currency').lastValue,
                    symbol: form.getForm().findField('symbol').lastValue,
                    secType: form.getForm().findField('secType').lastValue,
                    fillPrice: form.getForm().findField('fillPrice').lastValue,
                    fillDate: Ext.Date.format(new Date(form.getForm().findField('fillDate').lastValue), 'Y-m-d H:i:s.u'),
                    comment: form.getForm().findField('comment').lastValue
                },
                success: function(response, opts) {
                    window.close();
                }
            });
        }
    },

    onCancelAddExecution: function(button, e, options) {
        this.lookupReference('executionAddWindow').close();
    },

    onDeleteExecution: function(button) {
        var me = this,
            execution = button.getWidgetRecord().data;

        Ext.Msg.show({
            title:'Delete Execution?',
            message: 'Are you sure you want to delete the execution, id=' + execution.id + '?',
            buttons: Ext.Msg.YESNO,
            icon: Ext.Msg.QUESTION,
            fn: function(btn) {
                if (btn === 'yes') {
                    Ext.Ajax.request({
                        method: 'DELETE',
                        url: HanGui.common.Definitions.urlPrefixReport + '/reports/' + me.reportId + '/executions/' + execution.id
                    });
                }
            }
        });
    },

    reloadStatisticsAndCharts: function() {
        var me = this,
            statistics = me.getStore('statistics'),
            charts = me.getStore('charts'),
            interval = me.lookupReference('intervalCombo').getValue(),
            tradeType  = me.lookupReference('tradeTypeCombo').getValue(),
            secType  = me.lookupReference('secTypeCombo').getValue(),
            currency  = me.lookupReference('currencyCombo').getValue(),
            underlying =  me.lookupReference('underlyingCombo').getValue(),
            statisticsPaging = me.lookupReference('statisticsPaging');

        me.lookupReference('chartsButton').toggle(false);

        statistics.getProxy().setExtraParam('tradeType', tradeType);
        charts.getProxy().setExtraParam('tradeType', tradeType);

        statistics.getProxy().setExtraParam('secType', secType);
        charts.getProxy().setExtraParam('secType', secType);

        statistics.getProxy().setExtraParam('currency', currency);
        charts.getProxy().setExtraParam('currency', currency);

        statistics.getProxy().setExtraParam('underlying', underlying);
        charts.getProxy().setExtraParam('underlying', underlying);

        if (statisticsPaging.getStore().isLoaded()) {
            statisticsPaging.moveFirst();
        } else {
            statistics.load(function(records, operation, success) {
                if (success) {
                    console.log('reloaded statistics for report, id=' + me.reportId + ', interval=' + interval + ', underlying=' + underlying);
                }
            });
        }
        charts.load(function(records, operation, success) {
            if (success) {
                console.log('reloaded charts for report, id=' + me.reportId + ', interval=' + interval + ', underlying=' + underlying);
            }
        });
    },

    createCharts: function(tabPanel, newCard, oldCard, eOpts) {
        var me = this,
            statistics = me.getStore('charts'),

            cumulativePl = [],
            profitLoss = [],
            numberExecutions = [],
            numberOpenedClosed = [],
            numberWinnersLosers = [],
            pctWinners = [],
            bigWinnerLoser = [],
            plWinnersLosers = [];

        if (!Ext.get('hpb_c1')) {
            return;
        }
        cumulativePl.push(['Date', 'Cumulative PL']);
        profitLoss.push(['Date', 'PL', { role: 'style' }]);
        numberExecutions.push(['Date', 'Executions']);
        numberOpenedClosed.push(['Date', 'Opened', 'Closed']);
        numberWinnersLosers.push(['Date', 'Winners', 'Losers']);
        pctWinners.push(['Date', 'Percent Winners']);
        bigWinnerLoser.push(['Date', 'Big Winner', 'Big Loser']);
        plWinnersLosers.push(['Date', 'Winners Profit', 'Losers Loss']);

        statistics.each(function (record, id) {
            var rd = record.data;

            cumulativePl.push([new Date(rd.periodDate), rd.cumulProfitLoss]);
            profitLoss.push([new Date(rd.periodDate), rd.profitLoss, (rd.profitLoss > 0 ? 'green' : (rd.profitLoss == 0 ? 'white' : 'red'))]);
            numberExecutions.push([new Date(rd.periodDate), rd.numExecs]);
            numberOpenedClosed.push([new Date(rd.periodDate), rd.numOpened, rd.numClosed]);
            numberWinnersLosers.push([new Date(rd.periodDate), rd.numWinners, rd.numLosers]);
            pctWinners.push([new Date(rd.periodDate), rd.pctWinners]);
            bigWinnerLoser.push([new Date(rd.periodDate), rd.bigWinner, rd.bigLoser]);
            plWinnersLosers.push([new Date(rd.periodDate), rd.winnersProfit, rd.losersLoss]);
        });

        GoogleChart.ceateLineChart(cumulativePl, 'Cumulative PL', 'hpb_c1');
        GoogleChart.ceateColumnChart(profitLoss, 'Profit/Loss', 'hpb_c2');
        GoogleChart.ceateColumnChart(numberExecutions, 'Number Executions', 'hpb_c3');
        GoogleChart.ceateColumnChart(numberOpenedClosed, 'Number Opened/Closed', 'hpb_c4');
        GoogleChart.ceateColumnChart(numberWinnersLosers, 'Number Winners/Losers', 'hpb_c5');
        GoogleChart.ceateColumnChart(pctWinners, 'Percent Winners', 'hpb_c6');
        GoogleChart.ceateColumnChart(bigWinnerLoser, 'Biggest Winner/Loser', 'hpb_c7');
        GoogleChart.ceateColumnChart(plWinnersLosers, 'Winners Profit/Losers Loss', 'hpb_c8');
    },

    setGlyphs: function() {
        var me = this;

        me.lookupReference('executionsPanel').setGlyph(HanGui.common.Glyphs.getGlyph('orderedlist'));
        me.lookupReference('tradesPanel').setGlyph(HanGui.common.Glyphs.getGlyph('money'));
        me.lookupReference('statisticsPanel').setGlyph(HanGui.common.Glyphs.getGlyph('barchart'));
    },

    onChartsToggle: function(button, pressed, eOpts) {
        var me = this,
            chartsContainer = me.lookupReference('chartsContainer');

        if (pressed) {
            chartsContainer.setVisible(true);
            me.createCharts();
        } else {
            chartsContainer.setVisible(false);
        }
    }
});