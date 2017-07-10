/*
 * Copyright 2017-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uni.wue.app.fingerprint;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

/**
 * Skeletal ONOS UI Custom-View message handler.
 */
public class FingerprintsUiMessageHandler extends UiMessageHandler {

    private static final String SAMPLE_CUSTOM_DATA_REQ = "fingerprintsDataRequest";
    private static final String SAMPLE_CUSTOM_DATA_RESP = "fingerprintsDataResponse";

    private static final String REGISTER_CUSTOM_DATA_REQ = "registerFingerprintDataRequest";
    private static final String REGISTER_CUSTOM_DATA_RESP = "registerFingerprintDataResponse";

    private static final String UNREGISTER_CUSTOM_DATA_REQ = "unregisterFingerprintDataRequest";
    private static final String UNREGISTER_CUSTOM_DATA_RESP = "unregisterFingerprintDataResponse";

    private static final String TRUSTED_FINGERPRINTS = "trustedFingerprints";
    private static final String PENDING_FINGERPRINTS = "pendingFingerprints";
    private static final String BLOCKED_FINGERPRINTS = "blockedFingerprints";

    private static final String DEVICE_ID = "deviceId";
    private static final String ID = "id";
    private static final String STATIC_SUMMARY = "staticSummary";
    private static final String DYNAMIC_SUMMARY = "dynamicSummary";
    private static final String STATUS_FINGERPRINT = "statusFingerprint";

    private static final String MSG = "msg";
    private static final String MSG_TYPE = "msgType";


    private final Logger log = LoggerFactory.getLogger(getClass());

    protected FingerprintManagerComponent fingerprintManagerComponent;

    protected DeviceService deviceService;


    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new SampleCustomDataRequestHandler(),
                new RegisterCustomDataRequestHandler(),
                new UnregisterCustomDataRequestHandler()
        );
    }

    // handler for sample data requests
    private final class SampleCustomDataRequestHandler extends RequestHandler {

        private SampleCustomDataRequestHandler() {
            super(SAMPLE_CUSTOM_DATA_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            log.info("Outputting Fingerprints");

            fingerprintManagerComponent = get(FingerprintManagerComponent.class);


            ObjectNode result = objectNode();

            ArrayNode pendingFingerprints = arrayNode();
            result.set(PENDING_FINGERPRINTS, pendingFingerprints);

            ArrayNode trustedFingerprints = arrayNode();
            result.set(TRUSTED_FINGERPRINTS, trustedFingerprints);

            ArrayNode blockedFingerprints = arrayNode();
            result.set(BLOCKED_FINGERPRINTS, blockedFingerprints);

            Map<DeviceId,Fingerprint> penFPs = fingerprintManagerComponent.getPendingFingerprints();
            for(DeviceId dev: penFPs.keySet()) {
                log.info("Outputting Pending Fingerprint");
                pendingFingerprints.add(populatePendingRow(penFPs.get(dev)));
            }

            Map<DeviceId,Fingerprint> blockedFPs = fingerprintManagerComponent.getBlockedFingerprints();
            for(DeviceId dev: blockedFPs.keySet()) {
                log.info("Outputting Blocked Fingerprint");
                blockedFingerprints.add(populateBlockedRow(blockedFPs.get(dev)));
            }

            Map<DeviceId,Fingerprint> trustedFPs = fingerprintManagerComponent.getTrustedFingerprints();
            for(DeviceId dev: trustedFPs.keySet()) {
                log.info("Outputting Trusted Fingerprint");
                trustedFingerprints.add(populateTrustedRow(trustedFPs.get(dev)));
            }

            sendMessage(SAMPLE_CUSTOM_DATA_RESP, result);
        }
    }



    // handler for sample data requests
    private final class RegisterCustomDataRequestHandler extends RequestHandler {

        private RegisterCustomDataRequestHandler() {
            super(REGISTER_CUSTOM_DATA_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            log.info("Register Call");

            fingerprintManagerComponent = get(FingerprintManagerComponent.class);

            String strDevId = string(payload, ID, "(none)");
            DeviceId deviceId =  DeviceId.deviceId(strDevId);

            fingerprintManagerComponent.register(deviceId);

            //Assign to view
            ObjectNode result = objectNode();
            result.put(MSG,"Device successfully registered!");
            result.put(MSG_TYPE,"success");

            sendMessage(REGISTER_CUSTOM_DATA_RESP, result);
        }
    }

    // handler for sample data requests
    private final class UnregisterCustomDataRequestHandler extends RequestHandler {

        private UnregisterCustomDataRequestHandler() {
            super(UNREGISTER_CUSTOM_DATA_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            log.info("Unregister Call");

            fingerprintManagerComponent = get(FingerprintManagerComponent.class);

            String strDevId = string(payload, ID, "(none)");
            DeviceId deviceId =  DeviceId.deviceId(strDevId);

            fingerprintManagerComponent.unregister(deviceId);

            //Assign to view
            ObjectNode result = objectNode();
            result.put(MSG,"Device successfully unregistered!");
            result.put(MSG_TYPE,"success");

            sendMessage(UNREGISTER_CUSTOM_DATA_RESP, result);
        }
    }



    //populate table row
    private ObjectNode populatePendingRow(Fingerprint fp){
        return objectNode()
                .put(DEVICE_ID,fp.getDevice().toString())
                .put(STATUS_FINGERPRINT, fp.getStatus().toString());
    }

    //populate table row
    private ObjectNode populateTrustedRow(Fingerprint fp){
        return objectNode()
                .put(DEVICE_ID,fp.getDevice().toString())
                .put(STATIC_SUMMARY, fp.getStaticFingerprintSummary())
                .put(DYNAMIC_SUMMARY, fp.getDynamicFingerprintSummary());
    }

    //populate table row
    private ObjectNode populateBlockedRow(Fingerprint fp){
        return objectNode()
                .put(DEVICE_ID,fp.getDevice().toString())
                .put(STATIC_SUMMARY, fp.getStaticFingerprintSummary())
                .put(DYNAMIC_SUMMARY, fp.getDynamicFingerprintSummary());

    }
}
