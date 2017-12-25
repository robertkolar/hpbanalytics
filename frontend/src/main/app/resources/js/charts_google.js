var GoogleChart = (function() {
    var my = {};

    my.ceateLineChart = function(items, title, divEl) {
        var data = google.visualization.arrayToDataTable(items);
        var options = {
            title: title,
            fontSize: 12,
            legend: 'none'
        };
        var chart = new google.visualization.LineChart(document.getElementById(divEl));
        chart.draw(data, options);
    };

    my.ceateColumnChart = function(items, title, divEl) {
        var data = google.visualization.arrayToDataTable(items);
        var options = {
            fontSize: 12,
            title : title,
            legend: 'none',
            series: [
                {color: 'green'},
                {color: 'red'}
            ]
        };
        var chart = new google.visualization.ColumnChart(document.getElementById(divEl));
        chart.draw(data, options);
    };

    return my;
}());