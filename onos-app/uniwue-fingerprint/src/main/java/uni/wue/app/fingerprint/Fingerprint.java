package uni.wue.app.fingerprint;

import org.onlab.packet.ChassisId;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import sun.security.provider.MD5;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by nicholas on 30.04.17.
 */
public class Fingerprint {

    public enum FingerprintStatus{
        PENDING,PENDING_DEVICE,PENDING_FLOWMOD,TRUSTED,BLOCKED;
    }

    private DeviceId deviceId;

    private String staticFingerprintSummary;

    private String dynamicFingerprintSummary;

    private FingerprintStatus status;


    //Device Attributes
    private ChassisId chassisId;

    private String hwVersion;

    private String swVersion;

    private String manufacturer;

    private String serial;

    private String managementAddress;

    private String ofVersion;

    private int nrPorts;

    private List<String> ports;

    //FlowMods
    private float m1000Mean;

    private float m1000StdDev;

    private float m1000Median;

    private float m1000Min;

    private float m1000Max;

    private List<Float> m1000;


    public Fingerprint() {
        this.deviceId = null;
        this.staticFingerprintSummary = "-";
        this.dynamicFingerprintSummary = "-";
        this.status = FingerprintStatus.PENDING;
        this.ports = new LinkedList<String>();
    }

    public Fingerprint(DeviceId deviceId) {
        this();
        this.deviceId = deviceId;
    }

    public boolean equalsDynamic(Fingerprint fp){
        return true;
    }

    public void computeStaticSummary(){
        String concat = this.toString();

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch(NoSuchAlgorithmException ex) {
            staticFingerprintSummary = concat;
            return;
        }
        byte[] md5sum = md.digest(concat.getBytes());
        staticFingerprintSummary = String.format("%032X", new BigInteger(1, md5sum));
    }

    public String toString(){
        return deviceId.toString()+chassisId.toString()+hwVersion+swVersion+manufacturer+serial+managementAddress+ofVersion+nrPorts+ Arrays.toString(ports.toArray());
    }


    //Getters & Setters

    public DeviceId getDevice() {
        return deviceId;
    }

    public void setDeviceId(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    public String getStaticFingerprintSummary() {
        return staticFingerprintSummary;
    }

    public void setStaticFingerprintSummary(String staticFingerprintSummary) {
        this.staticFingerprintSummary = staticFingerprintSummary;
    }

    public String getDynamicFingerprintSummary() {
        return dynamicFingerprintSummary;
    }

    public void setDynamicFingerprintSummary(String dynamicFingerprintSummary) {
        this.dynamicFingerprintSummary = dynamicFingerprintSummary;
    }

    public FingerprintStatus getStatus() {
        return status;
    }

    public void setStatus(FingerprintStatus status) {
        this.status = status;
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public ChassisId getChassisId() {
        return chassisId;
    }

    public void setChassisId(ChassisId chassisId) {
        this.chassisId = chassisId;
    }

    public String getHwVersion() {
        return hwVersion;
    }

    public void setHwVersion(String hwVersion) {
        this.hwVersion = hwVersion;
    }

    public String getSwVersion() {
        return swVersion;
    }

    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public int getNrPorts() {
        return nrPorts;
    }

    public void setNrPorts(int nrPorts) {
        this.nrPorts = nrPorts;
    }

    public List<String> getPorts() {
        return ports;
    }

    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    public String getManagementAddress() {
        return managementAddress;
    }

    public void setManagementAddress(String managementAddress) {
        this.managementAddress = managementAddress;
    }

    public String getOfVersion() {
        return ofVersion;
    }

    public void setOfVersion(String ofVersion) {
        this.ofVersion = ofVersion;
    }
}
