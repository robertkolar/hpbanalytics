/**
 * Created by robertk on 9/6/15.
 */
Ext.define('HanGui.view.report.window.ExecutionAddWindow', {
    extend: 'Ext.window.Window',

    requires: [
        'HanGui.common.Glyphs',
        'HanGui.view.report.ReportController'
    ],
    layout: 'fit',
    closable: false,
    closeAction: 'destroy',
    modal: true,
    width: 350,

    items: [{
        xtype: 'form',
        reference: 'executionAddForm',
        bodyPadding: 15,
        layout: 'anchor',
        defaults: {
            anchor: '100%',
            padding: 10,
            allowBlank: false,
            msgTarget: 'side',
            labelWidth: 80
        },
        items: [{
            xtype: 'textfield',
            fieldLabel: 'Origin',
            name: 'origin',
            minLength: 1,
            maxLength: 20,
            value: 'INTERNAL'
        }, {
            xtype: 'textfield',
            fieldLabel: 'Ref ID',
            name: 'referenceId',
            minLength: 1,
            maxLength: 20,
            value: 'N/A'
        }, {
            xtype: 'combobox',
            name: 'action',
            editable: false,
            queryMode: 'local',
            fieldLabel: 'Action',
            store: Ext.create('Ext.data.Store', {
                fields: ['text'],
                data: [{"text": "BUY"}, {"text": "SELL"}]
            }),
            value: 'BUY'
        }, {
            xtype: 'numberfield',
            fieldLabel: 'Quantity',
            name: 'quantity',
            allowDecimals: false,
            minValue: 0,
            value: 100
        }, {
            xtype: 'textfield',
            fieldLabel: 'Underlying',
            name: 'underlying',
            minLength: 1,
            maxLength: 10,
            value: 'SPY'
        }, {
            xtype: 'textfield',
            fieldLabel: 'Symbol',
            name: 'symbol',
            minLength: 1,
            maxLength: 30,
            value: 'SPY'
        }, {
            xtype: 'textfield',
            fieldLabel: 'Currency',
            name: 'currency',
            minLength: 1,
            maxLength: 10,
            value: 'USD'
        }, {
            xtype: 'combobox',
            name: 'secType',
            editable: false,
            queryMode: 'local',
            fieldLabel: 'Sec Type',
            store: Ext.create('Ext.data.Store', {
                fields: ['text'],
                data: [{"text": "STK"}, {"text": "OPT"}, {"text": "FUT"}, {"text": "CASH"}, {"text": "CFD"}]
            }),
            value: 'STK'
        }, {
            xtype: 'numberfield',
            fieldLabel: 'Fill Price',
            name: 'fillPrice',
            decimalPrecision: 5,
            minValue: 0,
            value: 0.01
        }, {
            xtype: 'datefield',
            fieldLabel: 'Fill Date',
            name: 'fillDate',
            format: 'm/d/Y H:i:s.u',
            listeners: {
                beforerender: function(datefield, eOpts) {
                    datefield.setValue(new Date());
                }
            }
        }, {
            xtype: 'textfield',
            fieldLabel: 'Comment',
            name: 'comment',
            maxLength: 255,
            allowBlank: true,
            value: 'MANUAL'
        }],
        dockedItems: [{
            xtype: 'toolbar',
            dock: 'bottom',
            ui: 'footer',
            layout: {
                pack: 'end',
                type: 'hbox'
            },
            items: [{
                xtype: 'button',
                text: 'Submit',
                margin: '5 0 5 0',
                listeners: {
                    click: 'onSubmitAddExecution',
                    beforerender: function(c, eOpts) {
                        c.setGlyph(HanGui.common.Glyphs.getGlyph('save'));
                    }
                }
            }, {
                xtype: 'button',
                text: 'Cancel',
                margin: '5 10 5 10',
                listeners: {
                    click: 'onCancelAddExecution',
                    beforerender: function(c, eOpts) {
                        c.setGlyph(HanGui.common.Glyphs.getGlyph('cancel'));
                    }
                }
            }]
        }]
    }]
});