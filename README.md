# 2017-im-fingerprint-onos-app

## Abstract
Software-defined Networking (SDN) provides an increased flexibility and cost savings by separating the data from the control plane. Despite these benefits, this separation also results in a greater attack surface as new devices and protocols are deployed. OpenFlow is one of these protocols and enables the communication between the switch and the controller. Ideally this connection takes place over an encrypted TLS channel, but as this feature is marked optional, it is not supported by all devices. This allows an attacker to eavesdrop and alter the communication, hence resulting in a comprised network. In this work, we demonstrate a new approach for authentication based on device fingerprinting to enhance the security in scenarios, where cryptographic mechanisms are unavailable.

## Install
Build with Maven
```
cd onos-app/
cd uniwue-fingerprint/
mvn clean
```

Install app
```
onos-app localhost install target/uniwue-fingerprint-1.0-SNAPSHOT.oar
```

Login to ONOS Webinterface
