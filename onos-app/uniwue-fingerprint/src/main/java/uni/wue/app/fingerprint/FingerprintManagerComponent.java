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

import org.apache.commons.collections.map.HashedMap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
@Service(value = FingerprintManagerComponent.class)
public class FingerprintManagerComponent{

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ApplicationId appId;

    private Map<DeviceId,Fingerprint> pendingFingerprints;
    private Map<DeviceId,Fingerprint> trustedFingerprints;
    private Map<DeviceId,Fingerprint> blockedFingerprints;



    private DeviceListener deviceListener;


    public FingerprintManagerComponent() {
        pendingFingerprints = new HashMap<DeviceId,Fingerprint>();
        trustedFingerprints = new HashMap<DeviceId,Fingerprint>();
        blockedFingerprints = new HashMap<DeviceId,Fingerprint>();
    }

    @Activate
    protected void activate() {
        log.info("FP Manager Started");
        appId = coreService.getAppId("uni.wue.app.fingerprint");

        deviceListener = new FingerprintDeviceListener();
        ((FingerprintDeviceListener)deviceListener).setFingerprintManager(this);
        ((FingerprintDeviceListener)deviceListener).setDeviceService(deviceService);


        //register services
        deviceService.addListener(deviceListener);

        //init devices already online
        for(Device dev: deviceService.getAvailableDevices(Device.Type.SWITCH)){
            log.info(dev.id().toString());
            ((FingerprintDeviceListener)deviceListener).extractDeviceInfo(dev);
        }
    }

    @Deactivate
    protected void deactivate() {
        log.info("FP Manager Stopped");
        deviceService.removeListener(deviceListener);
        //unblock all devices
        for(Fingerprint fp: blockedFingerprints.values()){
            unblockDevice(fp);
            blockedFingerprints.remove(fp.getDeviceId());
        }

    }


    public void initDynamicFP(Fingerprint fp){
        fp.setDynamicFingerprintSummary("1000-106-65-130");
        dynamicFingerprintCompleted(fp.getDeviceId());
    }


    //Functions
    public void staticFingerprintCompleted(DeviceId devId){
        log.info("FPM Static Completed");
        //get Fingerprint
        Fingerprint fp = null;
        if(pendingFingerprints.containsKey(devId)) {
            fp = pendingFingerprints.get(devId);
        } else {
            log.info("FPM Completed: Key not contained. Something went wrong!");
            return;
        }

        log.info(fp.toString());

        //check if this fingerprint is valid
        if(trustedFingerprints.containsKey(devId)
                && trustedFingerprints.get(devId).getStaticFingerprintSummary().equals(fp.getStaticFingerprintSummary())){
            //init dynamic check
            fp.setStatus(Fingerprint.FingerprintStatus.PENDING_FLOWMOD);
            initDynamicFP(fp);
            log.info("Static match for trusted!");
        } else if(blockedFingerprints.containsKey(devId)
                && blockedFingerprints.get(devId).getStaticFingerprintSummary().equals(fp.getStaticFingerprintSummary())){ //check if it is blocked
            //remove from pending all clear
            pendingFingerprints.remove(devId);
            log.info("Static match for blocked!");
        } else {
            //add to blocked by default
            fp.setStatus(Fingerprint.FingerprintStatus.BLOCKED);
            blockedFingerprints.put(devId,fp);
            pendingFingerprints.remove(devId);
            blockDevice(fp);
            log.info("Static do not match for blocked!");

        }
    }


    //Functions
    public void dynamicFingerprintCompleted(DeviceId devId){
        log.info("FPM Dynamic Completed");
        //get Fingerprint
        Fingerprint fp = null;
        if(pendingFingerprints.containsKey(devId)) {
            fp = pendingFingerprints.get(devId);
        } else {
            log.info("FPM Completed: Key not contained. Something went wrong!");
            return;
        }

        //check if this fingerprint is valid
        if(trustedFingerprints.containsKey(devId)
                && trustedFingerprints.get(devId).getStaticFingerprintSummary().equals(fp.getStaticFingerprintSummary())
                && trustedFingerprints.get(devId).equalsDynamic(fp)){
            //all clear device authenticated
            pendingFingerprints.remove(devId);
            log.info("Static & Dynamic match for trusted!");
        } else if(blockedFingerprints.containsKey(devId)
                && blockedFingerprints.get(devId).getStaticFingerprintSummary().equals(fp.getStaticFingerprintSummary())
                && trustedFingerprints.get(devId).equalsDynamic(fp)){ //check if it is blocked
            //remove from pending all clear
            pendingFingerprints.remove(devId);
            log.info("Static & Dynamic match for blocked!");
        } else {
            //add to blocked by default
            fp.setStatus(Fingerprint.FingerprintStatus.BLOCKED);
            blockedFingerprints.put(devId,fp);
            pendingFingerprints.remove(devId);
            blockDevice(fp);
            log.info("Static & Dynamic do not match for trusted or blocked!");
        }
    }



    public void register(DeviceId devId) {
        //get Fingerprint
        Fingerprint fp = null;
        if(blockedFingerprints.containsKey(devId)) {
            fp = blockedFingerprints.get(devId);
        } else {
            log.info("FPM Register: Key not contained. Something went wrong!");
            return;
        }

        fp.setStatus(Fingerprint.FingerprintStatus.TRUSTED);
        //add to trusted
        fp.setDynamicFingerprintSummary("1000-106-65-130");

        trustedFingerprints.put(devId,fp);
        blockedFingerprints.remove(devId);

        unblockDevice(fp);
    }

    public void unregister(DeviceId devId){
        //get Fingerprint
        Fingerprint fp = null;
        if(trustedFingerprints.containsKey(devId)) {
            fp = trustedFingerprints.get(devId);
        } else {
            log.info("FPM Unregister: Key not contained. Something went wrong!");
            return;
        }

        fp.setStatus(Fingerprint.FingerprintStatus.BLOCKED);
        //add to trusted
        blockedFingerprints.put(devId,fp);
        trustedFingerprints.remove(devId);
        blockDevice(fp);
    }

    public void blockDevice(Fingerprint fp){
        String cmd = "iptables -A INPUT -s "+ fp.getManagementAddress() +"  -p tcp --destination-port 6633 -j DROP";
        log.info("Blocking: " + fp.getManagementAddress());
        try {
            java.lang.Runtime.getRuntime().exec(cmd);
        }catch(IOException ex){
            log.info("Block Device IO Exception");
        }
    }

    public void unblockDevice(Fingerprint fp){
        String cmd = "iptables -D INPUT -s "+ fp.getManagementAddress() +" -p tcp --destination-port 6633 -j DROP";
        log.info("Unblocking: " + fp.getManagementAddress());

        try {
            java.lang.Runtime.getRuntime().exec(cmd);
        }catch(IOException ex){
            log.info("Unblock Device IO Exception");
        }
    }

    //Getters & Setters
    public Map<DeviceId, Fingerprint> getPendingFingerprints() {
        return pendingFingerprints;
    }

    public Map<DeviceId, Fingerprint> getTrustedFingerprints() {
        return trustedFingerprints;
    }

    public Map<DeviceId, Fingerprint> getBlockedFingerprints() {
        return blockedFingerprints;
    }
}
