/**
 * Created by robertk on 9/6/15.
 */
Ext.define('HanGui.common.Util', {
    statics: {
        showErrorMsg: function(text) {
            Ext.Msg.show({
                title: 'Error!',
                msg: text,
                icon: Ext.Msg.ERROR,
                buttons: Ext.Msg.OK
            });
        },

        showToast: function(text) {
            Ext.toast({
                html: text,
                closable: false,
                align: 't',
                slideInDuration: 400,
                minWidth: 400
            });
        }
    }
});