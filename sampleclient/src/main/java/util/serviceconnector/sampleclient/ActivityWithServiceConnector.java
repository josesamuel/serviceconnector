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
import util.serviceconnecor.service.IEchoService;

/**
 * Sample activity that connects with a service and calls a method
 */
public class ActivityWithServiceConnector extends Activity {

    private static final String TAG = ActivityWithServiceConnector.class.getSimpleName();

    @ServiceInfo(serviceIntent = "util.serviceconnector.ECHO_SERVICE")
    private IEchoService mEchoService;


    @ServiceConnectionCallback
    public void onServiceConnectionChanged(String serviceIntent, boolean connected) throws RemoteException {
        echoMessage("Test");
    }

    /**
     * Call a method on remote interface
     */
    private void echoMessage(String message) throws RemoteException {
        Log.v(TAG, mEchoService.echo(message));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_without_service_connector);
        ServiceConnector.bind(this, this);
    }
}
