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

import java.util.Collection;
import java.util.Map;

/**
 * Skeletal ONOS UI Custom-View message handler.
 */
public class FingerprintsDetailsUiMessageHandler extends UiMessageHandler {

    private static final String SAMPLE_CUSTOM_DATA_REQ = "detailsFingerprintsDataRequest";
    private static final String SAMPLE_CUSTOM_DATA_RESP = "detailsFingerprintsDataResponse";


    private static final String DEVICE_ID = "deviceId";
    private static final String ID = "id";
    private static final String STATIC_SUMMARY = "staticSummary";
    private static final String DYNAMIC_SUMMARY = "dynamicSummary";
    private static final String STATUS_FINGERPRINT = "statusFingerprint";
    private static final String CHASSIS_ID = "chassisId";
    private static final String HW_VERSION = "hwVersion";
    private static final String SW_VERSION = "swVersion";
    private static final String MANUFACTURER = "manufacturer";
    private static final String SERIAL = "serial";
    private static final String MANAGEMENT_ADDRESS = "managementAddress";
    private static final String OF_VERSION = "ofVersion";
    private static final String NR_PORTS = "nrPorts";

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected FingerprintManagerComponent fingerprintManagerComponent;

    protected DeviceService deviceService;


    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new DetailsSampleCustomDataRequestHandler()
        );
    }

    // handler for sample data requests
    private final class DetailsSampleCustomDataRequestHandler extends RequestHandler {

        private DetailsSampleCustomDataRequestHandler() {
            super(SAMPLE_CUSTOM_DATA_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            log.info("Outputting Fingerprints");

            fingerprintManagerComponent = get(FingerprintManagerComponent.class);

            Fingerprint fp = null;

            String strDevId = string(payload, ID, "(none)");
            DeviceId deviceId =  DeviceId.deviceId(strDevId);

            log.info("DevIdStr:"+strDevId);

            if(fp==null && fingerprintManagerComponent.getBlockedFingerprints().containsKey(deviceId))
                fp = fingerprintManagerComponent.getBlockedFingerprints().get(deviceId);

            if(fp==null && fingerprintManagerComponent.getTrustedFingerprints().containsKey(deviceId))
                fp = fingerprintManagerComponent.getTrustedFingerprints().get(deviceId);


            ObjectNode result = populateDetails(fp);
            sendMessage(SAMPLE_CUSTOM_DATA_RESP, result);
        }
    }


    //populate table row
    private ObjectNode populateDetails(Fingerprint fp){
        return objectNode()
                .put(DEVICE_ID,fp.getDevice().toString())
                .put(STATUS_FINGERPRINT, fp.getStatus().toString())
                .put(STATIC_SUMMARY, fp.getStaticFingerprintSummary())
                .put(DYNAMIC_SUMMARY, fp.getDynamicFingerprintSummary())
                .put(CHASSIS_ID, fp.getChassisId().toString())
                .put(HW_VERSION, fp.getHwVersion())
                .put(SW_VERSION, fp.getSwVersion())
                .put(MANUFACTURER, fp.getManufacturer())
                .put(SERIAL, fp.getSerial())
                .put(MANAGEMENT_ADDRESS, fp.getManagementAddress())
                .put(OF_VERSION, fp.getOfVersion())
                .put(NR_PORTS, fp.getNrPorts());
    }
}
