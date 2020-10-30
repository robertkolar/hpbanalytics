/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.view.execution.window.ExecutionAddWindow', {
    extend: 'Ext.window.Window',

    requires: [
        'HanGui.common.Glyphs',
        'HanGui.view.execution.ExecutionController'
    ],
    reference: 'executionAddWindow',
    title: 'Add New Execution',
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
            fieldLabel: 'Reference',
            name: 'reference',
            value: 'manual add'
        }, {
            xtype: 'combobox',
            fieldLabel: 'Action',
            name: 'action',
            editable: false,
            queryMode: 'local',
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
            value: 1
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
            value: 'SPY201002P00336000'
        }, {
            xtype: 'combobox',
            name: 'secType',
            editable: false,
            queryMode: 'local',
            fieldLabel: 'Sec Type',
            store: Ext.create('Ext.data.Store', {
                fields: ['text'],
                data: [{"text": "OPT"}, {"text": "FOP"}, {"text": "FUT"}, {"text": "CFD"}, {"text": "STK"}]
            }),
            value: 'OPT'
        }, {
            xtype: 'textfield',
            fieldLabel: 'Currency',
            name: 'currency',
            minLength: 1,
            maxLength: 10,
            value: 'USD'
        }, {
            xtype: 'numberfield',
            fieldLabel: 'Multiplier',
            name: 'multiplier',
            allowDecimals: false,
            minValue: 1,
            value: 100
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
            format: 'm/d/Y H:i:s',
            listeners: {
                beforerender: function (datefield, eOpts) {
                    datefield.setValue(new Date());
                }
            }
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
