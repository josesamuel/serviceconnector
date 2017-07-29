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
public class ServiceHandler<T extends IInterface> {

    private static final String TAG = "ServiceConnector";
    private Context context;
    private boolean connected;
    private T service;
    private Class serviceStub;
    private String serviceIntent;
    private boolean bound;
    private boolean destroyed;
    private ServiceListener serviceListener;
    private ExecutorService executorService;

    /**
     * Service connection
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
            try {
                Method asInterfaceMethod = serviceStub.getMethod("asInterface", IBinder.class);
                synchronized (ServiceHandler.this) {
                    ServiceHandler.this.service = (T) asInterfaceMethod.invoke(serviceStub, serviceBinder);
                    connected = true;
                    ServiceHandler.this.notifyAll();
                }
                serviceBinder.linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        connectToService();
                    }
                }, 0);
                ServiceHandler.this.onServiceConnected();
            } catch (Exception ex) {
                Log.w(TAG, ex);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            synchronized (ServiceHandler.this) {
                ServiceHandler.this.service = null;
                connected = false;
            }
            ServiceHandler.this.onServiceDisconnected();
            connectToService();
        }
    };

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
    public ServiceHandler(final Context context, final String serviceIntent, Class<? extends IInterface> serviceClass,
                          ExecutorService executorService, ServiceListener serviceListener, boolean connect) {
        this.context = context;
        this.serviceStub = getStub(serviceClass);
        this.serviceIntent = serviceIntent;
        this.executorService = executorService;
        this.serviceListener = serviceListener;
        if (connect) {
            connectToService();
        }
    }


    /**
     * Returns true if this handler is connected with the service.
     */
    public synchronized boolean isConnected() {
        return connected;
    }

    /**
     * Returns the service intent
     */
    public final String getServiceIntent() {
        return serviceIntent;
    }

    /**
     * Override to know when service gets connected
     */
    protected void onServiceConnected() {
        if (serviceListener != null) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    serviceListener.onServiceConnected(getServiceIntent(), ServiceHandler.this);
                }
            });
        }
    }

    /**
     * Override to know when service gets disconnected
     */
    protected void onServiceDisconnected() {
        if (serviceListener != null) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    serviceListener.onServiceDisconnected(getServiceIntent(), ServiceHandler.this);
                }
            });
        }
    }

    /**
     * Returns the service interface
     */
    public final T getService() {
        return service;
    }

    /**
     * Destroys this connection
     */
    public void destroy() {
        destroyed = true;
        if (bound) {
            context.unbindService(serviceConnection);
            bound = false;
            connected = false;
            onServiceDisconnected();
        }
    }


    public void connectToService() {
        if (!connected && !destroyed) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (!destroyed) {
                        Intent sIntent = createExplicitFromImplicitIntent(context, new Intent(serviceIntent));
                        context.startService(sIntent);
                        bound = context.bindService(sIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                    }
                }
            });
        }
    }


    /**
     * Creates the intent to use to connect to service.
     */
    private Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        Intent explicitIntent = null;
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
            if (resolveInfo != null && resolveInfo.size() >= 1) {
                ResolveInfo serviceInfo = resolveInfo.get(0);
                String packageName = serviceInfo.serviceInfo.packageName;
                String className = serviceInfo.serviceInfo.name;
                ComponentName component = new ComponentName(packageName, className);
                explicitIntent = new Intent(implicitIntent);
                explicitIntent.setComponent(component);
            }
        }
        return explicitIntent;
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

}
