/**
 * The main application class. An instance of this class is created by app.js when it calls
 * Ext.application(). This is the ideal place to handle application launch and initialization
 * details.
 */
Ext.define('HanGui.Application', {
    extend: 'Ext.app.Application',

    requires: [
        'HanGui.view.main.Main'
    ],

    name: 'HPB Analytics',

    stores: [
    ],
    
    launch: function () {
        google.charts.load('current', {packages: ['corechart']});
        google.charts.setOnLoadCallback(console.log('Google charts loaded'));
        var link = document.createElement('link');
        link.type = 'image/ico';
        link.rel = 'icon';
        link.href = 'resources/images/favicon.ico';
        document.getElementsByTagName('head')[0].appendChild(link);

        var main = Ext.create('HanGui.view.main.Main');

        var viewport = Ext.create('Ext.container.Viewport', {
            layout: 'fit'
        });
        viewport.add(main);
    }
});
