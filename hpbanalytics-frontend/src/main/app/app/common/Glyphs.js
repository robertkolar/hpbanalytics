/**
 * Created by robertk on 9/6/15.
 */
Ext.define('HanGui.common.Glyphs', {
    singleton: true,

    config: {
        webFont: 'FontAwesome',
        add: 'xf067',
        edit: 'xf040',
        delete: 'xf1f8',
        save: 'xf00c',
        cancel: 'xf0e2',
        refresh: 'xf021',
        barchart: 'xf080',
        orderedlist: 'xf0cb',
        money: 'xf0d6',
        gear: 'xf013',
        destroy: 'xf1f8',
        logger: 'xf039',
        signal: 'xf012',
        report: 'xf201'
    },

    constructor: function(config) {
        this.initConfig(config);
    },

    getGlyph: function(glyph) {
        var me = this,
            font = me.getWebFont();
        if (typeof me.config[glyph] === 'undefined') {
            return false;
        }
        return me.config[glyph] + '@' + font;
    }
});