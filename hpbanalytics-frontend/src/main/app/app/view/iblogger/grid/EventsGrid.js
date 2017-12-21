/**
 * Created by robertk on 4/17/15.
 */
Ext.define('HanGui.view.iblogger.grid.EventsGrid', {
    extend: 'Ext.grid.Panel',

    requires: [
        'Ext.grid.column.Date'
    ],

    disableSelection: true,
    header: false,
    viewConfig: {
        stripeRows: true
    },
    columns: [{
        text: 'ID',
        width: 100,
        dataIndex: 'id',
        renderer: function(val, metadata, record) {
            return record.data['id'] + '/' + record.data['ibOrderDbId'];
        }
    }, {
        text: 'Event Date',
        width: 180,
        dataIndex: 'eventDate',
        xtype: 'datecolumn',
        format: 'm/d/Y H:i:s.u'
    }, {
        text: 'Status',
        width: 80,
        dataIndex: 'status',
        renderer: function(val, metadata, record) {
            metadata.style = 'background-color: ' + HanGui.common.Definitions.getIbOrderStatusColor(val) + '; color: white;';
            return val.toLowerCase();
        }
    }, {
        text: 'Price',
        flex: 1,
        dataIndex: 'price',
        align: 'right',
        renderer: function(val, metadata, record) {
            return (val ? Ext.util.Format.number(val, '0.00###') : '-');
        }
    }]
});