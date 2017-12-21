/**
 * Created by robertk on 4/11/15.
 */
Ext.define('HanGui.model.iblogger.IbOrderEvent', {
    extend: 'HanGui.model.iblogger.Base',

    fields: [
        {name: 'id', type: 'string'},
        {name: 'eventDate', type: 'date', dateFormat: 'time'},
        'status',
        'price',
        {name: 'ibOrderDbId', type: 'string', reference: {type: 'IbOrder', inverse: 'ibOrderEvents'}}
    ]
});