/**
 * Created by robertk on 12/29/2017.
 */
Ext.define('HanGui.model.ordtrack.Position', {
    extend: 'HanGui.model.ordtrack.Base',
    idProperty: 'conid',

    fields: [
        'accountId',
        'conid',
        'symbol',
        'underlyingSymbol',
        'currency',
        'exchange',
        'secType',
        'size'
    ]
});