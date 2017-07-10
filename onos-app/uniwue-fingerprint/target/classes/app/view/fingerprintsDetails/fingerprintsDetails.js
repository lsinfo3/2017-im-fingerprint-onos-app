// js for sample app custom view
(function () {
    'use strict';

    // injected refs
    var $log, $scope, wss, ks;


    var detailsDataReq = 'detailsFingerprintsDataRequest',
        detailsDataResp = 'detailsFingerprintsDataResponse';


    function addKeyBindings() {
        var map = {
            space: [getData, 'Fetch data from server'],

            _helpFormat: [
                ['space']
            ]
        };

        ks.keyBindings(map);
    }

function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

    function getData() {
        var loc = getParameterByName("devId");
        wss.sendEvent(detailsDataReq, {id: loc});
    }

    function respDataCb(data) {
        $scope.data = data;
        $scope.$apply();
    }



    angular.module('ovFingerprintsDetails', [])
        .controller('OvFingerprintsDetailsCtrl',
        ['$log', '$scope', 'WebSocketService', 'KeyService',

        function (_$log_, _$scope_, _wss_, _ks_) {
            $log = _$log_;
            $scope = _$scope_;
            wss = _wss_;
            ks = _ks_;

            var handlers = {};
            $scope.data = {};

            // data response handler
            handlers[detailsDataResp] = respDataCb;
            wss.bindHandlers(handlers);

            addKeyBindings();

            // custom click handler
            $scope.getData = getData;

            // get data the first time...
            getData();

            // cleanup
            $scope.$on('$destroy', function () {
                wss.unbindHandlers(handlers);
                ks.unbindKeys();
                $log.log('OvFingerprintsDetailsCtrl has been destroyed');
            });

            $log.log('OvFingerprintsDetailsCtrl has been created');
        }]);

}());
