package util.service;

import android.os.IInterface;
import android.util.Log;

import java.lang.reflect.Field;

import util.service.annotation.ServiceInfo;

/**
 * Wraps the field to bbe initialized which was annotated with {@link ServiceInfo}
 */
class ServiceFieldInfo implements ServiceConnectorListener {

    private static final String TAG = ServiceConnector.class.getSimpleName();
    private Field serviceField;
    private Object target;

    /**
     * Initialize a {@link ServiceFieldInfo} with given {@link Field} that
     * needs to be initializes, the target object
     */
    ServiceFieldInfo(Field serviceField, Object target) {
        this.serviceField = serviceField;
        this.target = target;
        if (!serviceField.isAccessible()) {
            serviceField.setAccessible(true);
        }
    }

    @Override
    public void onServiceConnected(String serviceIntent, IInterface serviceObject, ServiceConnector serviceConnector) {
        setServiceObject(serviceObject);
    }

    @Override
    public void onServiceDisconnected(String serviceIntent, ServiceConnector serviceConnector) {
        setServiceObject(null);
    }

    /**
     * Sets the field with the given object
     */
    private void setServiceObject(IInterface serviceObject) {
        try {
            serviceField.set(target, serviceObject);
        } catch (IllegalAccessException ex) {
            Log.w(TAG, "Unable to set the service object", ex);
        }

    }
}
