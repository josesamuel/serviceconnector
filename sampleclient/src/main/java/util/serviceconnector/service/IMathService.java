package util.serviceconnector.service;

import remoter.annotations.Remoter;

/**
 * Remoter interface
 */
@Remoter
public interface IMathService {

    /**
     * Returns the sum
     */
    int sum(int first, int second);

}
