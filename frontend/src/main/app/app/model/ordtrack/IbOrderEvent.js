/**
 * Created by robertk on 4/11/2015.
 */
Ext.define('HanGui.model.ordtrack.IbOrderEvent', {
    extend: 'HanGui.model.ordtrack.Base',

    fields: [
        {name: 'id', type: 'string'},
        {name: 'eventDate', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
        'status',
        'price',
        {name: 'ibOrderDbId', type: 'string', reference: {type: 'IbOrder', inverse: 'ibOrderEvents'}}
    ]
});