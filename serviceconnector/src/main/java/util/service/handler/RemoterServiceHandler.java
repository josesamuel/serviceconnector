package util.service.handler;


import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

/**
 * Takes care of connecting to a service of the given type
 *
 * @author jsam
 */
public class RemoterServiceHandler<T> extends AbstractServiceHandler {

    private static final String TAG = "ServiceConnector";


    //*************************************************************

    /**
     * Initialize this handler
     *
     * @param context         Context to use
     * @param serviceClass    The  class
     * @param serviceIntent   Intent of service to connect to.
     * @param serviceListener Listener to get callbacks.
     * @param connect         Whether to initiate connection
     */
    public RemoterServiceHandler(final Context context, final String serviceIntent, Class<T> serviceClass,
                                 ExecutorService executorService, ServiceListener serviceListener, boolean connect) {
        super(context, serviceIntent, serviceClass, executorService, serviceListener, connect);
    }


    @Override
    protected T initService(IBinder serviceBinder) {
        T service = null;
        try {
            Class proxyClass = Class.forName(getServiceClass().getName() + "_Proxy");
            service = (T) proxyClass.getConstructor(IBinder.class).newInstance(serviceBinder);

        } catch (Exception ex) {
            Log.w(TAG, "Error while initializing service instance", ex);
        }
        return service;
    }
}
