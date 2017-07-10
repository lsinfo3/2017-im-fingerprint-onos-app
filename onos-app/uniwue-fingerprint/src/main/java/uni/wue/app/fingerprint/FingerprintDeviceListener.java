package uni.wue.app.fingerprint;

import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;

import org.onosproject.net.device.DeviceService;


import java.util.Map;

/**
 * Created by nicholas on 30.04.17.
 */
public class FingerprintDeviceListener implements DeviceListener {


    protected FingerprintManagerComponent fingerprintManager;

    protected DeviceService deviceService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void event(DeviceEvent deviceEvent) {
        log.info("FP DL: Triggered");

        Device dev = deviceEvent.subject();
        extractDeviceInfo(dev);

    }

    @Override
    public boolean isRelevant(DeviceEvent event) {
        if((event.type() == DeviceEvent.Type.DEVICE_ADDED || event.type() == DeviceEvent.Type.DEVICE_AVAILABILITY_CHANGED)
                && event.subject() != null
                && event.subject().type() == Device.Type.SWITCH
                && deviceService.isAvailable(event.subject().id())){
            log.info("FP DL: Detected a New Switch");
            return true;
        }
        
        return false;
    }


    public void extractDeviceInfo(Device dev){
        //check if this device is not already pending
        DeviceId deviceId = dev.id();

        Fingerprint fp = null;

        Map<DeviceId,Fingerprint> pending = fingerprintManager.getPendingFingerprints();
        if(!pending.containsKey(deviceId)){
            log.info("FP DL: Extracting Info");
            fp = new Fingerprint(deviceId);
            pending.put(deviceId,fp);

            fp.setStatus(Fingerprint.FingerprintStatus.PENDING_DEVICE);
        } else {
            fp = pending.get(deviceId);
        }

        fp.setChassisId(dev.chassisId());
        fp.setHwVersion(dev.hwVersion());
        fp.setSwVersion(dev.swVersion());
        fp.setManufacturer(dev.manufacturer());
        fp.setSerial(dev.serialNumber());
        fp.setManagementAddress(dev.annotations().value("managementAddress"));
        fp.setOfVersion(dev.annotations().value("protocol"));
        for(Port port: deviceService.getPorts(dev.id())){
            fp.getPorts().add(port.annotations().value("portName")+":"+port.annotations().value("portMac"));
        }
        //compute static summary
        fp.computeStaticSummary();

        fingerprintManager.staticFingerprintCompleted(dev.id());

    }



    public void setFingerprintManager(FingerprintManagerComponent fingerprintManager) {
        this.fingerprintManager = fingerprintManager;
    }

    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
}
