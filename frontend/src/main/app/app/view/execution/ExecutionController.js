/**
 * Created by robertk on 8/24/2020.
 */
Ext.define('HanGui.view.execution.ExecutionController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.han-execution',

    init: function() {
        var me = this,
            executions = me.getStore('executions'),
            wsStatusField = me.lookupReference('wsStatus');

        if (executions) {
            executions.getProxy().setUrl(HanGui.common.Definitions.urlPrefix + '/execution');
            me.loadExecutions();
        }

        var socket  = new SockJS('/websocket');
        var stompClient = Stomp.over(socket);
        stompClient.debug = function(str) {
        };

        stompClient.connect({}, function(frame) {
            console.log('WS execution connected');
            wsStatusField.update('WS connected');
            wsStatusField.addCls('han-connected');

            stompClient.subscribe('/topic/execution', function(message) {
                if (message.body.startsWith('reloadRequest')) {
                    executions.reload();
                }
            });

        }, function() {
            console.log('WS execution disconnected');

            wsStatusField.update('WS disconnected');
            wsStatusField.removeCls('han-connected');
            wsStatusField.addCls('han-disconnected');
        });
    },

    loadExecutions: function() {
        var me = this,
            executions = me.getStore('executions');

        executions.load(function(records, operation, success) {
            if (success) {
                console.log('loaded executions');
            }
        });
    },

    onAddExecution: function(button, e, options) {
        var me = this;

        me.lookupReference('executionPanel').add(Ext.create('HanGui.view.execution.window.ExecutionAddWindow', {
            reference: 'executionAddWindow',
            title: 'Add New Execution'
        })).show();
    },

    onSubmitAddExecution: function(button, e, options) {
        var me = this,
            form = me.lookupReference('executionAddForm'),
            window = me.lookupReference('executionAddWindow');

        if (form && form.isValid()) {
            Ext.Ajax.request({
                method: 'POST',
                url: HanGui.common.Definitions.urlPrefix + '/execution',
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
                        url: HanGui.common.Definitions.urlPrefix + '/execution/' + execution.id
                    });
                }
            }
        });
    }
});
