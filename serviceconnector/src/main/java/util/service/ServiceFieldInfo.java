package util.service;

import android.os.IInterface;
import android.util.Log;

import java.lang.reflect.Field;

import util.service.annotation.ServiceInfo;

/**
 * Wraps the field to bbe initialized which was annotated with {@link ServiceInfo}
 */
class ServiceFieldInfo extends ServiceConnectorListener {

    private static final String TAG = ServiceConnector.class.getSimpleName();
    private Field serviceField;
    private Object target;

    /**
     * Initialize a {@link ServiceFieldInfo} with given {@link Field} that
     * needs to be initializes, the target object
     */
    ServiceFieldInfo(Field serviceField, Object target) {
        super(target);
        this.serviceField = serviceField;
        this.target = target;
        if (!serviceField.isAccessible()) {
            serviceField.setAccessible(true);
        }
    }

    @Override
    public void onServiceConnected(String serviceIntent, Object serviceObject, ServiceConnector serviceConnector) {
        setServiceObject(serviceObject);
    }

    @Override
    public void onServiceDisconnected(String serviceIntent, ServiceConnector serviceConnector) {
        setServiceObject(null);
    }

    @Override
    void onServiceConnectionFailed(String serviceIntent, Exception exception) {

    }

    /**
     * Sets the field with the given object
     */
    private void setServiceObject(Object serviceObject) {
        try {
            serviceField.set(target, serviceObject);
        } catch (IllegalAccessException ex) {
            Log.w(TAG, "Unable to set the service object", ex);
        }
    }
}
