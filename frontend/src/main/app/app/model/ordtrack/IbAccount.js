/**
 * Created by robertk on 4/11/2015.
 */
Ext.define('HanGui.model.ordtrack.IbAccount', {
    extend: 'HanGui.model.ordtrack.Base',
    idProperty: 'accountId',

    fields: [
        'accountId',
        'connected',
        'host',
        'port',
        'listen',
        'allowUpd',
        'stk',
        'fut',
        'opt',
        'fx',
        'cfd',
        'permittedClients',
        'permittedAccounts'
    ]
});