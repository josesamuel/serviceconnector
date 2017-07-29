package util.service;


import android.content.Context;
import android.os.IInterface;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.service.annotation.ServiceConnectionCallback;
import util.service.annotation.ServiceInfo;
import util.service.handler.ServiceHandler;
import util.service.handler.ServiceListener;


/**
 * ServiceConnector makes it easy to connect to remote android aidl services.
 * It manages the process of dealing with ServiceConnection leaving you to
 * simply focus on what to do with the service objects
 * <p>
 * To use a ServiceConnector,
 * <ol>
 * <li>
 * Declare the remote IInterface as instance fields, and annotate it with @ServiceInfo
 * ex:
 * <pre>
 * {@code
 *  @literal @ServiceInfo(serviceIntent="com.myintent.MY_SERVICE")
 *      private IMYService myService;
 * }
 * </pre>
 * </li>
 * <li>
 * (Optional) If you want to know when the service is connected/disconnected,
 * add callback methods and annotate them with @ServiceConnectionCallback
 * <pre>
 * {@code
 *  @literal @ServiceConnectionCallback
 *      public void onServiceConnectionChanged(String serviceIntent, boolean connected) {
 *      }
 * }
 * </pre>
 * </li>
 * <li>
 * Call {@link ServiceConnector#bind(Object, Context)}
 * ex:
 * </li>
 * </ol>
 *
 * @author jsam
 * @see util.service.ServiceConnector
 */
public final class ServiceConnector implements ServiceListener {

    private static final String TAG = "ServiceConnector";
    //singleton instance
    private static ServiceConnector serviceConnector;
    private ExecutorService executor;
    //map of service intent-servicehandler
    private Map<String, ServiceHandler> serviceHandlerMap;
    //map of serviceintent-servicefieldinfo
    private Map<String, List<ServiceFieldInfo>> serviceInfoMap;
    private List<ServiceListenerInfo> serviceCallbacks;


    //*************************************************************

    /**
     * Initialize the singleton instance of ServiceConnector
     */
    private ServiceConnector() {
        executor = Executors.newCachedThreadPool();
        serviceInfoMap = new HashMap<>();
        serviceHandlerMap = new HashMap<>();
        serviceCallbacks = new ArrayList<>();
    }

    /**
     * Userd to internally access the singleton instance, creating it if needed
     */
    private static synchronized ServiceConnector getInstance() {
        if (serviceConnector == null) {
            serviceConnector = new ServiceConnector();
        }
        return serviceConnector;
    }

    /**
     * Call this to process the given target object to look for any
     * {@link IInterface} fields annotated with {@link ServiceInfo},
     * and if so connects to those services.
     * Those fields will get initialized with the remote
     * {@link IInterface} service objects when the service is
     * successfully connected.
     *
     * @param target  The object to analyze
     * @param context Context used to connect to service
     */
    public static void bind(Object target, Context context) {
        getInstance().initListeners(target);
        getInstance().initServiceHandlers(target, context);
    }

    /**
     * Disconnects from all the services connected by this {@link ServiceConnector}
     */
    public static void unbind() {
        getInstance().disconnectServices();
    }

    /**
     * Returns true if all the annotated services are connected
     */
    public static boolean isAllConnected() {
        return getInstance().isAllServicesConnected();
    }

    /**
     * Returns true if connected with a service of the given intent
     *
     * @param serviceIntent The intent of service to check for
     */
    public static boolean isConnected(String serviceIntent) {
        return getInstance().isServiceConnected(serviceIntent);
    }


    /**
     * Blocks for up to the given timeout for the connection to the given service.
     *
     * @param timeout       Maximum timeout in ms to wait for. 0 Waits until service is connected
     * @param serviceIntent The service intent
     */
    public static void waitForConnected(long timeout, String serviceIntent) throws InterruptedException {
        getInstance().waitForServiceConnected(timeout, serviceIntent);
    }

    /**
     * Blocks for up to the given timeout for the connection with all the services
     *
     * @param timeout Maximum timeout in ms to wait for. 0 Waits until service is connected
     */
    public static void waitForAllConnected(long timeout) throws InterruptedException {
        getInstance().waitForAllServiceConnected(timeout);
    }


    /**
     * Blocks for given timeout until connected with the given service
     */
    private void waitForServiceConnected(long timeout, String serviceIntent) throws InterruptedException {
        ServiceHandler serviceHandler = serviceHandlerMap.get(serviceIntent);
        if (serviceHandler != null) {
            synchronized (serviceHandler) {
                if (!serviceHandler.isConnected()) {
                    serviceHandler.wait(timeout);
                }
            }
        }
    }

    /**
     * Blocks for given timeout until connected with all services
     */
    private synchronized void waitForAllServiceConnected(long timeout) throws InterruptedException {
        if (!isAllServicesConnected()) {
            wait(timeout);
        }
    }

    /**
     * Returns true if connected with all services
     */
    private boolean isAllServicesConnected() {
        for (ServiceHandler serviceHandler : serviceHandlerMap.values()) {
            if (!serviceHandler.isConnected()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if connected with the given service
     */
    private boolean isServiceConnected(String serviceIntent) {
        return serviceHandlerMap.containsKey(serviceIntent) && serviceHandlerMap.get(serviceIntent)
                                                                                .isConnected();
    }

    /**
     * Initialize the call back listeners
     */
    private void initListeners(Object target) {
        Class targetClass = target.getClass();
        Method[] methods = targetClass.getDeclaredMethods();

        for (Method method : methods) {
            ServiceConnectionCallback listenerInfo = method.getAnnotation(ServiceConnectionCallback.class);
            if (listenerInfo != null) {
                Class[] parameters = method.getParameterTypes();
                if (parameters.length == 2 && parameters[0].isAssignableFrom(String.class) && parameters[1].isAssignableFrom(boolean.class)) {
                    serviceCallbacks.add(new ServiceListenerInfo(method, target));
                } else {
                    Log.w(TAG, "Expected signature for listener method is (String, boolean");
                }
            }
        }
    }

    /**
     * Initialize service handlers to connect with actual service
     */
    private void initServiceHandlers(Object target, Context context) throws IllegalArgumentException {
        Class targetClass = target.getClass();
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            ServiceInfo serviceInfo = field.getAnnotation(ServiceInfo.class);
            if (serviceInfo != null) {
                if (IInterface.class.isAssignableFrom(field.getType())) {
                    addServiceHandler(serviceInfo, (Class<? extends IInterface>) field.getType(), context);
                    addFieldInfo(serviceInfo, field, target);
                } else {
                    throw new IllegalArgumentException(field.getName() + " is not a field of type IInterface");
                }
            }
        }
        connectServices();
    }

    /**
     * Create {@link ServiceHandler} if neccessary to connect to servie specified by given {@link ServiceInfo}
     */
    private void addServiceHandler(ServiceInfo serviceInfo, Class<? extends IInterface> serviceClass, Context context) {
        if (!serviceHandlerMap.containsKey(serviceInfo.serviceIntent())) {
            serviceHandlerMap.put(serviceInfo.serviceIntent(),
                                  new ServiceHandler<>(context, serviceInfo.serviceIntent(), serviceClass, executor, this, false));
        }
    }

    /**
     * Keep track of the fields to initialize
     */
    private void addFieldInfo(ServiceInfo serviceInfo, Field serviceField, Object target) {
        List<ServiceFieldInfo> serviceConnectors ;
        if (serviceInfoMap.containsKey(serviceInfo.serviceIntent())) {
            serviceConnectors = serviceInfoMap.get(serviceInfo.serviceIntent());
        } else {
            serviceConnectors = new ArrayList<>();
            serviceInfoMap.put(serviceInfo.serviceIntent(), serviceConnectors);
        }
        serviceConnectors.add(new ServiceFieldInfo(serviceField, target));
    }

    /**
     * Connect to services
     */
    private void connectServices() {
        for (ServiceHandler serviceHandler : serviceHandlerMap.values()) {
            serviceHandler.connectToService();
        }
    }

    /**
     * Disconnects all services
     */
    private void disconnectServices() {
        for (ServiceHandler serviceHandler : serviceHandlerMap.values()) {
            serviceHandler.destroy();
        }
        serviceInfoMap.clear();
        serviceHandlerMap.clear();
        serviceCallbacks.clear();
    }


    @Override
    public void onServiceConnected(String serviceIntent, ServiceHandler serviceHandler) {
        IInterface serviceObject = serviceHandler.getService();
        //initialize the fields
        for (ServiceConnectorListener serviceConnectorListener : serviceInfoMap.get(serviceIntent)) {
            serviceConnectorListener.onServiceConnected(serviceIntent, serviceObject, this);
        }
        //call back listener methods
        for (ServiceConnectorListener serviceConnectorListener : serviceCallbacks) {
            serviceConnectorListener.onServiceConnected(serviceIntent, serviceObject, this);
        }
        //unblock if any
        synchronized (this) {
            if (isAllServicesConnected()) {
                notifyAll();
            }
        }
    }

    @Override
    public void onServiceDisconnected(String serviceIntent, ServiceHandler serviceHandler) {
        for (ServiceConnectorListener serviceConnectorListener : serviceInfoMap.get(serviceIntent)) {
            serviceConnectorListener.onServiceDisconnected(serviceIntent, this);
        }
        for (ServiceConnectorListener serviceConnectorListener : serviceCallbacks) {
            serviceConnectorListener.onServiceDisconnected(serviceIntent, this);
        }
    }
}
