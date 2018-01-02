/**
 * Created by robertk on 12/29/2017.
 */
Ext.define('HanGui.model.iblogger.Position', {
    extend: 'HanGui.model.iblogger.Base',
    idProperty: 'symbol',

    fields: [
        'accountId',
        'symbol',
        'underlying',
        'currency',
        'exchange',
        'secType',
        'position',
        'avgCost',
        'lastPrice',
        'underlyingPrice'
    ]
});