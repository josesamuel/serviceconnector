package util.service.handler;

/**
 * Listener to receive service connection call backs
 *
 * @see AidlServiceHandler
 */
public interface ServiceListener {

    /**
     * Called when service is connected
     *
     * @param serviceIntent The intent of the service connected
     */
    void onServiceConnected(String serviceIntent, AbstractServiceHandler serviceHandler);

    /**
     * Called when service is dis connected
     *
     * @param serviceIntent The intent of the service dis connected
     */
    void onServiceDisconnected(String serviceIntent, AbstractServiceHandler serviceHandler);

    /**
     * Called when the service bind fails
     */
    void onServiceConnectionFailed(String serviceIntent, Exception exception);
}
