package util.service.handler;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Takes care of connecting to a service of the given type
 *
 * @author jsam
 */
public class AidlServiceHandler<T extends IInterface> extends AbstractServiceHandler {

    private static final String TAG = "ServiceConnector";
    private Class serviceStub;


    //*************************************************************

    /**
     * Initialize this handler
     *
     * @param context         Context to use
     * @param serviceClass    The {@link IInterface} class
     * @param serviceIntent   Intent of service to connect to.
     * @param serviceListener Listener to get callbacks.
     * @param connect         Whether to initiate connection
     */
    public AidlServiceHandler(final Context context, final String serviceIntent, Class<? extends IInterface> serviceClass,
                              ExecutorService executorService, ServiceListener serviceListener, boolean connect) {
        super(context, serviceIntent, serviceClass, executorService, serviceListener, connect);
        this.serviceStub = getStub(serviceClass);
    }


    private Class<? extends IInterface> getStub(Class serviceClass) {
        Class[] subClasses = serviceClass.getDeclaredClasses();
        for (Class subClass : subClasses) {
            if (subClass.getSimpleName()
                    .equals("Stub")) {
                return subClass;
            }
        }
        return null;
    }

    @Override
    protected T initService(IBinder serviceBinder) {
        T service = null;
        try {
            Method asInterfaceMethod = serviceStub.getMethod("asInterface", IBinder.class);
            service = (T) asInterfaceMethod.invoke(serviceStub, serviceBinder);

        } catch (Exception ex) {
            Log.w(TAG, "Error while initializing service instance");
        }
        return service;
    }
}
