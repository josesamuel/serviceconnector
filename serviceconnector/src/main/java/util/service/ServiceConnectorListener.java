package util.service;

import android.os.IInterface;

/**
 * Listener to get callbacks about service connection changes from a {@link ServiceConnector}
 *
 * @see ServiceConnector
 */
abstract class ServiceConnectorListener {

    private Object target;

    /**
     * Initialize the listener for the given target
     */
    protected ServiceConnectorListener(Object target) {
        this.target = target;
    }

    /**
     * Called when a service is connected
     *
     * @param serviceIntent    Service Intent
     * @param serviceObject    Service Object
     * @param serviceConnector ServiceConnector
     */
    abstract void onServiceConnected(String serviceIntent, Object serviceObject, ServiceConnector serviceConnector);

    /**
     * Called when service is disconnected
     *
     * @param serviceIntent    Service intent
     * @param serviceConnector ServiceConnector
     */
    abstract void onServiceDisconnected(String serviceIntent, ServiceConnector serviceConnector);

    /**
     * Returns if the given target is same as the target used by this
     */
    boolean isSameTarget(Object target) {
        return target == this.target;
    }
}
