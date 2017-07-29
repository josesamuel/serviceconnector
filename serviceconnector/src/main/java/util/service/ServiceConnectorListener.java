package util.service;

import android.os.IInterface;

/**
 * Listener to get callbacks about service connection changes from a {@link ServiceConnector}
 *
 * @see ServiceConnector
 */
interface ServiceConnectorListener {

    /**
     * Called when a service is connected
     *
     * @param serviceIntent    Service Intent
     * @param serviceObject    Service Object
     * @param serviceConnector ServiceConnector
     */
    void onServiceConnected(String serviceIntent, IInterface serviceObject, ServiceConnector serviceConnector);

    /**
     * Called when service is disconnected
     *
     * @param serviceIntent    Service intent
     * @param serviceConnector ServiceConnector
     */
    void onServiceDisconnected(String serviceIntent, ServiceConnector serviceConnector);

}
