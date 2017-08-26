package util.service;

import util.service.annotation.ServiceInfo;
import util.serviceconnector.service.*;

import static util.service.ServiceIntents.INTENT_ECHO_SERVICE;
import static util.service.ServiceIntents.INTENT_MATH_SERVICE;

/**
 * A target that is used to test for multiple services
 */
public class MultipleTestTarget extends ServiceTestTarget {

    @ServiceInfo(serviceIntent = INTENT_ECHO_SERVICE)
    private IEchoService echoService;

    @ServiceInfo(serviceIntent = INTENT_MATH_SERVICE)
    private IMathService mathService;

    @Override
    public boolean isServiceSet() {
        return echoService != null && mathService != null;
    }

}
