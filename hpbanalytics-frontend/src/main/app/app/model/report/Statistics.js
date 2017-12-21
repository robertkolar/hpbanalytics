/**
 * Created by robertk on 9/6/15.
 */
Ext.define('HanGui.model.report.Statistics', {
    extend: 'HanGui.model.report.Base',

    fields: [
        'periodDate',
        'numOpened',
        'numClosed',
        'numWinners',
        'numLosers',
        'maxWinner',
        'maxLoser',
        'winnersProfit',
        'losersLoss',
        'profitLoss',
        'cumulProfitLoss'
    ]
});