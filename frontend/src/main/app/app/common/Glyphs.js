/**
 * Created by robertk on 9/6/2015.
 */
Ext.define('HanGui.common.Glyphs', {
    singleton: true,

    config: {
        webFont: 'FontAwesome',
        refresh: 'xf021',
        download: 'xf019',
        play: 'xf04b',
        times: 'xf00d',
        list: 'xf03a',
        history: 'xf1da',
        chain: 'xf0c1',
        send: 'xf1d8',
        check: 'xf00c',
        orderedlist: 'xf0cb',
        barchart: 'xf080',
        add: 'xf067',
        edit: 'xf040',
        delete: 'xf1f8',
        save: 'xf00c',
        cancel: 'xf0e2',
        money: 'xf0d6',
        gear: 'xf013',
        signal: 'xf012'
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
