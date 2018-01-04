/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.common.Definitions', {
    statics: {
        urlPrefixReport: 'http://' + window.location.host + '/report',
        urlPrefixOrdTrack: 'http://' + window.location.host + '/ordtrack',

        getIbOrderStatusColor: function(status) {
            var statusColor;

            switch(status) {
                case 'SUBMITTED':   statusColor = 'blue';   break;
                case 'UPDATED':     statusColor = 'blue';   break;
                case 'CANCELLED':   statusColor = 'brown';  break;
                case 'FILLED':      statusColor = 'green';  break;
                case 'UNKNOWN':     statusColor = 'gray';   break;
            }
            return statusColor;
        }
    }
});