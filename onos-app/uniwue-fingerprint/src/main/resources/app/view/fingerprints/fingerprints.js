// js for sample app custom view
(function () {
    'use strict';

    // injected refs
    var $log, $scope, wss, ks;

    // constants
    var dataReq = 'fingerprintsDataRequest',
        dataResp = 'fingerprintsDataResponse';

    var registerDataReq = 'registerFingerprintDataRequest',
        registerDataResp = 'registerFingerprintDataResponse';

    var unregisterDataReq = 'unregisterFingerprintDataRequest',
            unregisterDataResp = 'unregisterFingerprintDataResponse';


    function addKeyBindings() {
        var map = {
            space: [getData, 'Fetch data from server'],

            _helpFormat: [
                ['space']
            ]
        };

        ks.keyBindings(map);
    }

    function getData() {
        wss.sendEvent(dataReq);
    }

    function respDataCb(data) {
        $scope.data = data;
        $scope.$apply();
    }


    function register(devId) {
        wss.sendEvent(registerDataReq, {id: devId});
        $log.debug('Got a click on:', devId);
        getData();
    }

    function details(devId) {
        $log.debug('Got a click on:', devId);

        location.href = '/onos/ui/index.html#/fingerprintsDetails?devId='+devId;
    }

     function registerRespDataCb(data) {
        $scope.msg = data;
        $scope.$apply();
        getData();
     }

     function unregister(devId) {
        wss.sendEvent(unregisterDataReq, {id: devId});
        $log.debug('Got a click on:', devId);
        getData();
      }

      function unregisterRespDataCb(data) {
        $scope.msg = data;
        $scope.$apply();
        getData();
      }


    angular.module('ovFingerprints', [])
        .controller('OvFingerprintsCtrl',
        ['$log', '$scope', 'WebSocketService', 'KeyService',

        function (_$log_, _$scope_, _wss_, _ks_) {
            $log = _$log_;
            $scope = _$scope_;
            wss = _wss_;
            ks = _ks_;

            var handlers = {};
            $scope.data = {};

            // data response handler
            handlers[dataResp] = respDataCb;
            handlers[registerDataResp] = registerRespDataCb;
            handlers[unregisterDataResp] = unregisterRespDataCb;
            wss.bindHandlers(handlers);

            addKeyBindings();

            // custom click handler
            $scope.getData = getData;
            $scope.register = register;
            $scope.unregister = unregister;
            $scope.details = details;

            // get data the first time...
            getData();

            // cleanup
            $scope.$on('$destroy', function () {
                wss.unbindHandlers(handlers);
                ks.unbindKeys();
                $log.log('OvFingerprintsCtrl has been destroyed');
            });

            $log.log('OvFingerprintsCtrl has been created');
        }]);

}());
