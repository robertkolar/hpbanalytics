/**
 * Created by robertk on 4/17/2015.
 */
Ext.define('HanGui.model.ordtrack.Base', {
    extend: 'Ext.data.Model',

    requires: [
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json',
        'Ext.data.writer.Json',
        'HanGui.common.Definitions',
        'HanGui.common.Util'
    ],

    schema: {
        id: 'ordtrack',
        namespace: 'HanGui.model.ordtrack',
        urlPrefix: HanGui.common.Definitions.urlPrefixOrdTrack,
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