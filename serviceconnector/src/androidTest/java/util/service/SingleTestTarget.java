package util.service;

import util.service.annotation.ServiceInfo;
import util.serviceconnecor.service.*;

import static util.service.ServiceIntents.INTENT_ECHO_SERVICE;

/**
 * A target that is used to test for a service
 */
public class SingleTestTarget extends ServiceTestTarget {

    @ServiceInfo(serviceIntent = INTENT_ECHO_SERVICE)
    private IEchoService echoService;

    @Override
    public boolean isServiceSet() {
        return echoService != null;
    }
}
