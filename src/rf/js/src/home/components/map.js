'use strict';

var React = require('react');

var Map = React.createBackboneClass({
    render: function() {
        return (
            <div className="map-container">
                <div id="map"></div>
            </div>
        );
    }
});

module.exports = Map;
