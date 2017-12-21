/**
 * Created by robertk on 4/17/15.
 */
Ext.define('HanGui.model.iblogger.Base', {
    extend: 'Ext.data.Model',

    requires: [
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json',
        'Ext.data.writer.Json',
        'HanGui.common.Definitions',
        'HanGui.common.Util'
    ],

    schema: {
        id: 'iblogger',
        namespace: 'HanGui.model.iblogger',
        urlPrefix: HanGui.common.Definitions.urlPrefixIbLogger,
        proxy: {
            type: 'ajax',
            actionMethods: {
                read: 'GET',
                update: 'PUT'
            },
            reader: {
                type: 'json',
                rootProperty: 'items',
                totalProperty: 'total'
            },
            writer: {
                type: 'json',
                writeAllFields: true,
                writeRecordId: true
            },
            listeners: {
                exception: function(proxy, response, operation) {
                    //HanGui.common.Util.showErrorMsg(response.responseText);
                }
            }
        }
    }
});