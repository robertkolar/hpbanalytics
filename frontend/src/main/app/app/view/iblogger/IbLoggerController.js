/**
 * Created by robertk on 4/17/2015.
 */
Ext.define('HanGui.view.iblogger.IbLoggerController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.han-iblogger',

    requires: [
        'Ext.Ajax',
        'HanGui.common.Definitions',
        'HanGui.view.iblogger.grid.EventsGrid',
        'HanGui.view.iblogger.window.EventsWindow'
    ],

    init: function() {
        var me = this,
            ibAccounts = me.getStore('ibAccounts'),
            ibOrders = me.getStore('ibOrders'),
            positions = me.getStore('positions'),
            accountsGrid = me.lookupReference('accountsGrid');

        if (ibAccounts) {
            ibAccounts.getProxy().setUrl(HanGui.common.Definitions.urlPrefixIbLogger + '/ibaccounts');
            ibAccounts.load(function (records, operation, success) {
                if (success) {
                    accountsGrid.setSelection(ibAccounts.first());
                }
            });
        }

        var socket  = new SockJS('/websocket');
        var stompClient = Stomp.over(socket);

        stompClient.connect({}, function(frame) {
            console.log("WS iblogger connected");

            stompClient.subscribe('/topic/iblogger', function(message) {
                console.log('WS message, content=' + message + ' --> reloading stores...');
                ibOrders.reload();
                positions.reload();
            });

        }, function() {
            console.log("WS iblogger disconnected");
        });
    },

    onAccountSelect: function(grid, record, index, eOpts) {
        var me = this,
            ibOrders = me.getStore('ibOrders'),
            positions = me.getStore('positions'),
            ordersPaging = me.lookupReference('ordersPaging'),
            positionsPaging = me.lookupReference('positionsPaging');

        me.ibAccountId = record.data.accountId;
        ibOrders.getProxy().setUrl(HanGui.common.Definitions.urlPrefixIbLogger + '/ibaccounts/' + me.ibAccountId  + '/iborders');
        positions.getProxy().setUrl(HanGui.common.Definitions.urlPrefixIbLogger + '/ibaccounts/' + me.ibAccountId  + '/positions');

        if (ordersPaging.getStore().isLoaded()) {
            ordersPaging.moveFirst();
        } else {
            ibOrders.load(function(records, operation, success) {
                if (success) {
                    console.log('reloaded ibOrders for ' + me.ibAccountId)
                }
            });
        }

        if (positionsPaging.getStore().isLoaded()) {
            positionsPaging.moveFirst();
        } else {
            positions.load(function(records, operation, success) {
                if (success) {
                    console.log('reloaded positions for ' + me.ibAccountId)
                }
            });
        }
    },

    showEvents: function (view, cell, cellIndex, record, row, rowIndex, e) {
        if (cellIndex != 2) {
            return;
        }
        var me = this;

        if (!me.eventsGrid) {
            me.eventsGrid =  Ext.create('HanGui.view.iblogger.grid.EventsGrid');
            me.eventsWindow = Ext.create('widget.han-iblogger-events-window');
            me.eventsWindow.add(me.eventsGrid);
        }
        var permId = record.get(record.getFields()[1].getName());
        me.eventsGrid.setStore(record.ibOrderEvents());
        me.eventsWindow.setTitle("IB Order Events, permId=" + permId);
        me.eventsWindow.show();
    },

    statusRenderer: function(val, metadata, record) {
        metadata.style = 'cursor: pointer; background-color: ' + HanGui.common.Definitions.getIbOrderStatusColor(val) + '; color: white;';
        return val.toLowerCase();
    },

    connectStatusRenderer: function(val, metadata, record) {
        if (metadata) {
            metadata.style = 'background-color: ' + (val ? 'green' : 'red') + '; color: white;';
        }
        return (val ? 'conn' : 'disconn');
    },

    connectIb: function(grid, rowIndex, colIndex) {
        this.connect(grid, rowIndex, colIndex, true);
    },

    disconnectIb: function(grid, rowIndex, colIndex) {
        this.connect(grid, rowIndex, colIndex, false);
    },

    connect: function(grid, rowIndex, colIndex, con) {
        var me = this,
            ibAccounts = me.getStore('ibAccounts'),
            accountId = grid.getStore().getAt(rowIndex).get('accountId'),
            box = Ext.MessageBox.wait(((con ? 'Connecting' : 'Disconnecting') + ' IB account ' + accountId), 'Action in progress');

        Ext.Ajax.request({
            method: 'PUT',
            url: HanGui.common.Definitions.urlPrefixIbLogger + '/ibaccounts/' + accountId + '/connect/' + (con ? 'true' : 'false'),
            success: function(response) {
                box.hide();
                grid.getStore().reload();
            },
            failure: function() {
                box.hide();
            }
        });
    },

    setGlyphs: function() {
        var me = this;

        me.lookupReference('ordersPanel').setGlyph(HanGui.common.Glyphs.getGlyph('orderedlist'));
        me.lookupReference('positionsPanel').setGlyph(HanGui.common.Glyphs.getGlyph('money'));
    }
});