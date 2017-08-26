package util.service;

import util.service.annotation.ServiceConnectionCallback;
import util.service.annotation.ServiceInfo;
import util.serviceconnector.service.IEchoService;


/**
 * A target that is used to test for a service
 */
public class ServiceTestTarget {


    @ServiceConnectionCallback
    public void onServiceCallback(String serviceIntent, boolean connected) {
    }

    public boolean isServiceSet() {
        return false;
    }

}
