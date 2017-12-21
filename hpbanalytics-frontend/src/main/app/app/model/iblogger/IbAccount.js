/**
 * Created by robertk on 4/11/15.
 */
Ext.define('HanGui.model.iblogger.IbAccount', {
    extend: 'HanGui.model.iblogger.Base',
    idProperty: 'accountId',

    fields: [
        'accountId',
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
        'permittedAccounts',
        {name: 'ibConnection', persist: false},
        {name: 'accounts', mapping: 'ibConnection.accounts', persist: false},
        {name: 'connected', mapping: 'ibConnection.connected', persist: false}
    ]
});