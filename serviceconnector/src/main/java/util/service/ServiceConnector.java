package util.service;


import android.content.Context;
import android.os.IInterface;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.service.annotation.ServiceConnectionCallback;
import util.service.annotation.ServiceConnectionFailureCallback;
import util.service.annotation.ServiceInfo;
import util.service.handler.AbstractServiceHandler;
import util.service.handler.AidlServiceHandler;
import util.service.handler.RemoterServiceHandler;
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
 * <pre><code>
 *  {@literal @}ServiceInfo(serviceIntent="com.myintent.MY_SERVICE")
 *   private IMYService myService;
 * </code></pre>
 * </li>
 * <li>
 * (Optional) If you want to know when the service is connected/disconnected,
 * add callback methods and annotate them with @ServiceConnectionCallback
 * <pre><code>
 *  {@literal @}ServiceConnectionCallback
 *  public void onServiceConnectionChanged(String serviceIntent, boolean connected) {
 *  }
 * </code></pre>
 * </li>
 * <li>
 * <pre><code>
 * Call {@linkplain ServiceConnector#bind(Object, Context)}
 * </code></pre>
 * </li>
 * </ol>
 *
 * @author jsam
 * @see util.service.ServiceConnector
 */
public final class ServiceConnector implements ServiceListener {

    private static final String TAG = "ServiceConnector";
    private static boolean ENABLE_DEBUG = false;
    //singleton instance
    private static ServiceConnector serviceConnector;
    private ExecutorService executor;
    //map of service intent-servicehandler
    private Map<String, AbstractServiceHandler> serviceHandlerMap;
    //map of serviceintent-servicefieldinfo
    private Map<String, List<ServiceFieldInfo>> serviceInfoMap;
    private List<ServiceListenerInfo> serviceCallbacks;
    private List<ServiceListenerInfo> serviceFailtureCallbacks;


    //*************************************************************

    /**
     * Initialize the singleton instance of ServiceConnector
     */
    private ServiceConnector() {
        executor = Executors.newCachedThreadPool();
        serviceInfoMap = new ConcurrentHashMap<>();
        serviceHandlerMap = new ConcurrentHashMap<>();
        serviceCallbacks = new CopyOnWriteArrayList<>();
        serviceFailtureCallbacks = new CopyOnWriteArrayList<>();
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
        getInstance().bindTarget(target, context);
    }

    /**
     * Disconnects from all the services bounded to this target.
     * Any services that are no more bounded to any other targets will
     * be disconnected.
     *
     * @param target The target used to bind.
     */
    public static void unbind(Object target) {
        getInstance().unbindTarget(target);
    }

    /**
     * Returns true if all the annotated services are connected.
     *
     * @return if all services are connected
     */
    public static boolean isAllConnected() {
        return getInstance().isAllServicesConnected();
    }

    /**
     * Returns true if connected with a service of the given intent.
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
     * Call to enable or disable debug logs
     *
     * @param enableDebug Enable or disable
     */
    public static void setEnableDebug(boolean enableDebug) {
        ENABLE_DEBUG = enableDebug;
    }


    /**
     * Blocks for given timeout until connected with the given service
     */
    private void waitForServiceConnected(long timeout, String serviceIntent) throws InterruptedException {
        AbstractServiceHandler serviceHandler = serviceHandlerMap.get(serviceIntent);
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
        for (AbstractServiceHandler serviceHandler : serviceHandlerMap.values()) {
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
        while (targetClass != Object.class) {
            initListeners(targetClass, target);
            targetClass = targetClass.getSuperclass();
        }
    }

    /**
     * Search for callbackmethods annotated with @{@link ServiceConnectionCallback}
     */
    private void initListeners(Class targetClass, Object target) {
        Method[] methods = targetClass.getDeclaredMethods();

        for (Method method : methods) {
            ServiceConnectionCallback listenerInfo = method.getAnnotation(ServiceConnectionCallback.class);
            if (listenerInfo != null) {
                Class[] parameters = method.getParameterTypes();
                if (parameters.length == 2 && parameters[0].isAssignableFrom(String.class) && parameters[1].isAssignableFrom(boolean.class)) {
                    serviceCallbacks.add(new ServiceListenerInfo(method, target));
                    log("Adding listener " + method.getName());
                } else {
                    Log.w(TAG, "Expected signature for listener method is (String, boolean");
                }
            }

            ServiceConnectionFailureCallback failureInfo = method.getAnnotation(ServiceConnectionFailureCallback.class);
            if (failureInfo != null) {
                Class[] parameters = method.getParameterTypes();
                if (parameters.length == 2 && parameters[0].isAssignableFrom(String.class) && parameters[1].isAssignableFrom(Exception.class)) {
                    serviceFailtureCallbacks.add(new ServiceListenerInfo(method, target));
                    log("Adding listener " + method.getName());
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
        while (targetClass != Object.class) {
            initServiceHandlers(targetClass, target, context);
            targetClass = targetClass.getSuperclass();
        }
        connectServices();
    }

    /**
     * Search for the fields marked with @{@link ServiceInfo}
     */
    private void initServiceHandlers(Class targetClass, Object target, Context context) throws IllegalArgumentException {
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            ServiceInfo serviceInfo = field.getAnnotation(ServiceInfo.class);
            if (serviceInfo != null) {
                if (IInterface.class.isAssignableFrom(field.getType())) {
                    addServiceHandler(serviceInfo, (Class<? extends IInterface>) field.getType(), context);
                    addFieldInfo(serviceInfo, field, target);
                } else if (isRemoter(field.getType())) {
                    addRemoterServiceHandler(serviceInfo, field.getType(), context);
                    addFieldInfo(serviceInfo, field, target);

                } else {
                    throw new IllegalArgumentException(field.getName() + " is not a field of type IInterface or Remoter");
                }
            }
        }
    }

    /**
     * Checks whether the given class type is annotated with Remoter
     */
    private boolean isRemoter(Class fieldType) {
        boolean remoter = false;
        try {
            Class.forName(fieldType.getName() + "_Proxy");
            remoter = true;
        } catch (Exception e) {
        }
        return remoter;
    }

    /**
     * Create {@link AidlServiceHandler} if neccessary to connect to servie specified by given {@link ServiceInfo}
     */
    private void addServiceHandler(ServiceInfo serviceInfo, Class<? extends IInterface> serviceClass, Context context) {
        if (!serviceHandlerMap.containsKey(serviceInfo.serviceIntent())) {
            serviceHandlerMap.put(serviceInfo.serviceIntent(),
                    new AidlServiceHandler<>(context, serviceInfo.serviceIntent(), serviceClass, executor, this, false));
        }
    }

    /**
     * Create {@link AidlServiceHandler} if neccessary to connect to servie specified by given {@link ServiceInfo}
     */
    private void addRemoterServiceHandler(ServiceInfo serviceInfo, Class serviceClass, Context context) {
        if (!serviceHandlerMap.containsKey(serviceInfo.serviceIntent())) {
            serviceHandlerMap.put(serviceInfo.serviceIntent(),
                    new RemoterServiceHandler<>(context, serviceInfo.serviceIntent(), serviceClass, executor, this, false));
        }
    }

    /**
     * Keep track of the fields to initialize
     */
    private void addFieldInfo(ServiceInfo serviceInfo, Field serviceField, Object target) {
        List<ServiceFieldInfo> serviceConnectors;
        if (serviceInfoMap.containsKey(serviceInfo.serviceIntent())) {
            serviceConnectors = serviceInfoMap.get(serviceInfo.serviceIntent());
        } else {
            serviceConnectors = new ArrayList<>();
            serviceInfoMap.put(serviceInfo.serviceIntent(), serviceConnectors);
        }
        ServiceFieldInfo serviceFieldInfo = new ServiceFieldInfo(serviceField, target);
        serviceConnectors.add(serviceFieldInfo);
        notifyIfAllreadyConneced(serviceFieldInfo, target, serviceInfo.serviceIntent());
        log("Adding service field " + serviceField.getName());
    }

    /**
     * Sets the field and notify if service is already connected
     */
    private void notifyIfAllreadyConneced(ServiceFieldInfo serviceFieldInfo, Object target, String serviceIntent) {
        AbstractServiceHandler serviceHandler = serviceHandlerMap.get(serviceIntent);
        if (serviceHandler != null && serviceHandler.isConnected()) {
            serviceFieldInfo.onServiceConnected(serviceIntent, serviceHandler.getService(), this);
            //call back listener methods
            for (ServiceConnectorListener serviceConnectorListener : serviceCallbacks) {
                if (serviceConnectorListener.isSameTarget(target)) {
                    serviceConnectorListener.onServiceConnected(serviceIntent, serviceHandler.getService(), this);
                }
            }
        }
    }

    /**
     * Connect to services
     */
    private void connectServices() {
        log("Connecting with services");
        for (AbstractServiceHandler serviceHandler : serviceHandlerMap.values()) {
            serviceHandler.connectToService();
        }
    }

    /**
     * Binds to the given target, extracting the service fields to be initialized
     * and the callback methods to be called.
     */
    private void bindTarget(final Object target, final Context context) {
        initListeners(target);
        initServiceHandlers(target, context);
    }

    /**
     * Unbind services from the given target.
     *
     * @see #unbind(Object)
     */
    private void unbindTarget(Object target) {
        int listenerSize;

        //remove the service callbacks for same target
		listenerSize = serviceCallbacks.size();
        for (int i = listenerSize - 1; i >= 0; i--) {
            ServiceListenerInfo serviceListenerInfo = serviceCallbacks.get(i);
            if (serviceListenerInfo.isSameTarget(target)) {
                serviceCallbacks.remove(i);
            }
        }
        //remove the failure callbacks for same target
        listenerSize = serviceFailtureCallbacks.size();
        for (int i = listenerSize - 1; i >= 0; i--) {
            ServiceListenerInfo serviceListenerInfo = serviceFailtureCallbacks.get(i);
            if (serviceListenerInfo.isSameTarget(target)) {
                serviceFailtureCallbacks.remove(i);
            }
        }

        for (String serviceIntent : serviceInfoMap.keySet()) {
            //reset the service field for the same targets
            List<ServiceFieldInfo> serviceFieldInfoList = serviceInfoMap.get(serviceIntent);
            int size = serviceFieldInfoList.size();
            for (int i = size - 1; i >= 0; i--) {
                ServiceFieldInfo serviceFieldInfo = serviceFieldInfoList.get(i);
                if (serviceFieldInfo.isSameTarget(target)) {
                    serviceFieldInfo.onServiceDisconnected(serviceIntent, this);
                    serviceFieldInfoList.remove(i);
                }
            }
            if (serviceFieldInfoList.isEmpty()) {
                serviceHandlerMap.get(serviceIntent)
                        .destroy();
                serviceHandlerMap.remove(serviceIntent);
                serviceInfoMap.remove(serviceIntent);
            }
        }
    }


    @Override
    public void onServiceConnected(String serviceIntent, AbstractServiceHandler serviceHandler) {
        log("Service Connected " + serviceIntent);
        Object serviceObject = serviceHandler.getService();
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
    public void onServiceDisconnected(String serviceIntent, AbstractServiceHandler serviceHandler) {
        log("Service DisConnected " + serviceIntent);
        List<ServiceFieldInfo> serviceFieldInfoList = serviceInfoMap.get(serviceIntent);
        if (serviceFieldInfoList != null) {
            for (ServiceConnectorListener serviceConnectorListener : serviceFieldInfoList) {
                serviceConnectorListener.onServiceDisconnected(serviceIntent, this);
            }
        }
        for (ServiceConnectorListener serviceConnectorListener : serviceCallbacks) {
            serviceConnectorListener.onServiceDisconnected(serviceIntent, this);
        }
    }

    @Override
    public void onServiceConnectionFailed(String serviceIntent, Exception exception) {
        for (ServiceConnectorListener serviceConnectorListener : serviceFailtureCallbacks) {
            serviceConnectorListener.onServiceConnectionFailed(serviceIntent, exception);
        }
    }

    /**
     * Logs the message if enabled
     */
    private void log(String message) {
        if (ENABLE_DEBUG) {
            Log.v(TAG, message);
        }
    }

}
