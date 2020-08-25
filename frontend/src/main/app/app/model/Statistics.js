/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.model.Statistics', {
    extend: 'HanGui.model.Base',

    fields: [
        {name: 'periodDate', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
        'numExecs',
        'numOpened',
        'numClosed',
        'numWinners',
        'numLosers',
        'pctWinners',
        'bigWinner',
        'bigLoser',
        'winnersProfit',
        'losersLoss',
        'profitLoss',
        'cumulProfitLoss'
    ]
});
