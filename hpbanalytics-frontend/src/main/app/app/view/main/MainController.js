/**
 * This class is the main view for the application. It is specified in app.js as the
 * "autoCreateViewport" property. That setting automatically applies the "viewport"
 * plugin to promote that instance of this class to the body element.
 *
 */
Ext.define('HanGui.view.main.MainController', {
    extend: 'Ext.app.ViewController',

    requires: [
        'Ext.window.MessageBox'
    ],

    alias: 'controller.main',

    setGlyphs: function() {
        var me = this;

        me.lookupReference('ibLoggerPanel').setGlyph(HanGui.common.Glyphs.getGlyph('logger'));
        me.lookupReference('reportPanel').setGlyph(HanGui.common.Glyphs.getGlyph('report'));
    }
});
