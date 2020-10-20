/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.view.trade.window.TradeCloseWindow', {
    extend: 'Ext.window.Window',

    requires: [
        'HanGui.common.Glyphs'
    ],

    layout: 'fit',
    closable: false,
    closeAction: 'destroy',
    modal: true,
    width: 350,

    items: [{
        xtype: 'form',
        reference: 'tradeCloseForm',
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
            xtype: 'combobox',
            fieldLabel: 'Reference',
            name: 'reference',
            editable: false,
            queryMode: 'local',
            store: Ext.create('Ext.data.Store', {
                fields: ['text'],
                data: [{"text": "expire"}, {"text": "assign"}, {"text": "manual close"}]
            }),
            value: 'manual close trade'
        }, {
            xtype: 'datefield',
            reference: 'closeDate',
            fieldLabel: 'Close Date',
            name: 'closeDate',
            format: 'm/d/Y H:i:s'
        }, {
            xtype: 'numberfield',
            reference: 'closePrice',
            fieldLabel: 'Close Price',
            name: 'closePrice',
            decimalPrecision: 5,
            minValue: 0.0,
            value: 0.01
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
                    click: 'onSubmitCloseTrade',
                    beforerender: function(c, eOpts) {
                        c.setGlyph(HanGui.common.Glyphs.getGlyph('save'));
                    }
                }
            }, {
                xtype: 'button',
                text: 'Cancel',
                margin: '5 10 5 10',
                listeners: {
                    click: 'onCancelCloseTrade',
                    beforerender: function(c, eOpts) {
                        c.setGlyph(HanGui.common.Glyphs.getGlyph('cancel'));
                    }
                }
            }]
        }]
    }]
});
