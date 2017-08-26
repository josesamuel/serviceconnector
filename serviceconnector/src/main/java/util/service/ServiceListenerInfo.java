package util.service;

import android.os.IInterface;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Wraps the method that is annotated with  {@link util.service.annotation.ServiceConnectionCallback}
 */
class ServiceListenerInfo extends ServiceConnectorListener {

    private static final String TAG = ServiceConnector.class.getSimpleName();
    private Method listenerMethod;
    private Object target;

    /**
     * Initialize a {@link ServiceListenerInfo} with the method
     * to be called, and the target object
     */
    ServiceListenerInfo(Method listenerMethod, Object target) {
        super(target);
        this.listenerMethod = listenerMethod;
        this.target = target;
        if (!listenerMethod.isAccessible()) {
            listenerMethod.setAccessible(true);
        }
    }


    @Override
    public void onServiceConnected(String serviceIntent, Object serviceObject, ServiceConnector serviceConnector) {
        try {
            listenerMethod.invoke(target, serviceIntent, true);
        } catch (Exception ex) {
            Log.w(TAG, "Unable to call the listener method", ex);
        }
    }

    @Override
    public void onServiceDisconnected(String serviceIntent, ServiceConnector serviceConnector) {
        try {
            listenerMethod.invoke(target, serviceIntent, false);
        } catch (Exception ex) {
            Log.w(TAG, "Unable to call the listener method", ex);
        }
    }
}
