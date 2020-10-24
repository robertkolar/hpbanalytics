/**
 * Created by robertk on 10/15/2015.
 */
Ext.define('HanGui.view.statistics.StatisticsController', {
    extend: 'Ext.app.ViewController',

    requires: [
        'HanGui.common.Definitions',
        'Ext.data.proxy.Proxy'
    ],

    alias: 'controller.han-statistics',

    init: function() {
        var me = this,
            statistics = me.getStore('statistics'),
            charts = me.getStore('charts'),
            wsStatusField = me.lookupReference('wsStatus');

        if (statistics) {
            statistics.getProxy().setUrl(HanGui.common.Definitions.urlPrefix + '/statistics');
            charts.getProxy().setUrl(HanGui.common.Definitions.urlPrefix + '/statistics/charts');
            me.reloadStatisticsAndCharts();
        }

        me.prepareUnderlyingCombo();
        me.prepareIfiYearMonthCombos();

        var socket  = new SockJS('/websocket');
        var stompClient = Stomp.over(socket);
        stompClient.debug = function(str) {
        };

        stompClient.connect({}, function(frame) {
            console.log('WS statistics connected');
            wsStatusField.update('WS connected');
            wsStatusField.addCls('han-connected');

            stompClient.subscribe('/topic/statistics', function(message) {
                if (message.body.startsWith('reloadRequest')) {
                    statistics.reload();
                }
            });

        }, function() {
            console.log('WS statistics disconnected');

            wsStatusField.update('WS disconnected');
            wsStatusField.removeCls('han-connected');
            wsStatusField.addCls('han-disconnected');
        });
    },

    reloadStatisticsAndCharts: function() {
        var me = this,
            statistics = me.getStore('statistics'),
            charts = me.getStore('charts'),
            statisticsProxy = statistics.getProxy(),
            chartsProxy = charts.getProxy(),
            interval = me.lookupReference('intervalCombo').getValue(),
            tradeType  = me.lookupReference('tradeTypeCombo').getValue(),
            secType  = me.lookupReference('secTypeCombo').getValue(),
            currency  = me.lookupReference('currencyCombo').getValue(),
            underlying =  me.lookupReference('underlyingCombo').getValue(),
            statisticsPaging = me.lookupReference('statisticsPaging');

        me.lookupReference('chartsButton').toggle(false);

        statisticsProxy.setExtraParam('interval', interval);
        chartsProxy.setExtraParam('interval', interval);

        statisticsProxy.setExtraParam('tradeType', tradeType);
        chartsProxy.setExtraParam('tradeType', tradeType);

        statisticsProxy.setExtraParam('secType', secType);
        chartsProxy.setExtraParam('secType', secType);

        statisticsProxy.setExtraParam('currency', currency);
        chartsProxy.setExtraParam('currency', currency);

        statisticsProxy.setExtraParam('underlying', underlying);
        chartsProxy.setExtraParam('underlying', underlying);

        if (statisticsPaging.getStore().isLoaded()) {
            statisticsPaging.moveFirst();
        } else {
            statistics.load(function(records, operation, success) {
                if (success) {
                    console.log('reloaded statistics for interval=' + interval + ', underlying=' + underlying);
                }
            });
        }
        charts.load(function(records, operation, success) {
            if (success) {
                console.log('reloaded charts for interval=' + interval + ', underlying=' + underlying);
            }
        });
    },

    prepareUnderlyingCombo: function() {
        var me = this,
            underlyingCombo =  me.lookupReference('underlyingCombo'),
            openOnlyCheckbox = me.lookupReference('openOnlyCheckbox');

        Ext.Ajax.request({
            method: 'GET',
            url: HanGui.common.Definitions.urlPrefix + '/statistics/underlyings',
            params: {'openOnly': openOnlyCheckbox.getValue()},

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
    },

    prepareIfiYearMonthCombos: function() {
        var me = this,
            ifiYearCombo =  me.lookupReference('ifiYearCombo'),
            ifiEndMonthCombo =  me.lookupReference('ifiEndMonthCombo');

        Ext.Ajax.request({
            method: 'GET',
            url: HanGui.common.Definitions.urlPrefix + '/statistics/ifi/years',

            success: function(response, opts) {
                // years
                var ifiYears = Ext.decode(response.responseText);
                var ifiYearsData = [];
                for (var i = 0; i < ifiYears.length; i++) {
                    ifiYearsData.push([ifiYears[i]]);
                }
                ifiYearCombo.getStore().loadData(ifiYearsData);
                var defaultYear = ifiYearCombo.getStore().getAt(ifiYearsData.length - 1);
                ifiYearCombo.setValue(defaultYear);

                // months
                var ifiEndMonthsData = [];
                for (var m = 1; m <= 12; m++) {
                    ifiEndMonthsData.push([m]);
                }
                ifiEndMonthCombo.getStore().loadData(ifiEndMonthsData);
                var defaultEndMonth = ifiEndMonthCombo.getStore().getAt(ifiEndMonthsData.length - 1);
                ifiEndMonthCombo.setValue(defaultEndMonth);
            }
        });
    },

    onCalculateStatistics: function(button, evt) {
        var me = this,
            interval = me.lookupReference('intervalCombo').getValue(),
            tradeType =  me.lookupReference('tradeTypeCombo').getValue(),
            secType =  me.lookupReference('secTypeCombo').getValue(),
            currency =  me.lookupReference('currencyCombo').getValue(),
            underlying =  me.lookupReference('underlyingCombo').getValue();

        Ext.Ajax.request({
            method: 'POST',
            url: HanGui.common.Definitions.urlPrefix + '/statistics',
            jsonData: {
                interval: interval,
                tradeType: tradeType,
                secType: secType,
                currency: currency,
                underlying: underlying
            },

            success: function(response, opts) {
                me.reloadStatisticsAndCharts();
            }
        });
    },

    onDownloadIfiReport: function(button, evt) {
        var me = this,
            year = me.lookupReference('ifiYearCombo').getValue(),
            endMonth = me.lookupReference('ifiEndMonthCombo').getValue(),
            tradeType =  me.lookupReference('ifiTradeTypeCombo').getValue();

        Ext.Ajax.request({
            method: 'GET',
            url: HanGui.common.Definitions.urlPrefix + '/statistics/ifi/csv',
            params: {
                year: year,
                endMonth: endMonth,
                tradeType: tradeType
            },

            success: function(response, opts) {
                var content = response.responseText;

                var filename;
                if (endMonth !== 12) {
                    var endMonthStr = '' + endMonth;
                    var padMonth = '00';
                    endMonthStr = padMonth.substring(0, padMonth.length - endMonthStr.length) + endMonthStr;
                    filename = 'IFI_' + year + '_' + endMonthStr + '_' + tradeType.toLowerCase() + '.csv';
                } else {
                    filename = 'IFI_' + year + '_'  + tradeType.toLowerCase() + '.csv';
                }

                var blob = new Blob([content], {
                    type: 'text/plain;charset=utf-8'
                });
                saveAs(blob, filename);
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

    onChartsToggle: function(button, pressed, eOpts) {
        var me = this,
            chartsContainer = me.lookupReference('chartsContainer');

        if (pressed) {
            chartsContainer.setVisible(true);
            me.createCharts();
        } else {
            chartsContainer.setVisible(false);
        }
    },

    profitLossRenderer: function(val, metadata, record) {
        metadata.style = val < 0 ? 'color: red;' : 'color: green;';
        return Ext.util.Format.number(val, '0.00');
    }
});
