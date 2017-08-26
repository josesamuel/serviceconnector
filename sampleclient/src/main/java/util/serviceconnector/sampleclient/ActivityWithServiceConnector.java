package util.serviceconnector.sampleclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import util.service.ServiceConnector;
import util.service.annotation.ServiceConnectionCallback;
import util.service.annotation.ServiceInfo;
import util.serviceconnector.service.IEchoService;
import util.serviceconnector.service.IMathService;

/**
 * Sample activity that connects with a service and calls a method
 */
public class ActivityWithServiceConnector extends Activity {

    private static final String TAG = ActivityWithServiceConnector.class.getSimpleName();
    private static final String INTENT_ECHO_SERVICE = "util.serviceconnector.ECHO_SERVICE";
    private static final String INTENT_MATH_SERVICE = "util.serviceconnector.MATH_SERVICE";


    //AIDL service
    @ServiceInfo(serviceIntent = INTENT_ECHO_SERVICE)
    private IEchoService mEchoService;

    //service of type @Remoter
    @ServiceInfo(serviceIntent = INTENT_MATH_SERVICE)
    private IMathService mMathService;


    @ServiceConnectionCallback
    public void onServiceConnectionChanged(String serviceIntent, boolean connected) throws RemoteException {
        if (serviceIntent.equals(INTENT_ECHO_SERVICE)) {
            Log.v(TAG, "Echo service initialized");
            echoMessage("Test");
        } else if (serviceIntent.equals(INTENT_MATH_SERVICE)) {
            Log.v(TAG, "Math service initialized");
            doMath();
        }
    }

    /**
     * Call a method on remote interface
     */
    private void echoMessage(String message) throws RemoteException {
        Log.v(TAG, mEchoService.echo(message));
    }

    /**
     * Call a method on remote interface
     */
    private void doMath() throws RemoteException {
        Log.v(TAG, "1 + 1 = " + mMathService.sum(1, 1));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_without_service_connector);
        ServiceConnector.bind(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServiceConnector.unbind(this);
    }
}
