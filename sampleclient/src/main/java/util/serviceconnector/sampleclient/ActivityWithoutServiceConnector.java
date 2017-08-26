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

import util.serviceconnector.service.IEchoService;

/**
 * Sample activity that connects with a service and calls a method
 */
public class ActivityWithoutServiceConnector extends Activity {

    private static final String TAG = ActivityWithoutServiceConnector.class.getSimpleName();

    //remote service interface
    private IEchoService mEchoService;

    /**
     * Service connection that gets call back when service is connected
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mEchoService = IEchoService.Stub.asInterface(service);
            try {
                echoMessage("Test");
            } catch (RemoteException exception) {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mEchoService = null;
        }
    };


    /**
     * Connect with the remote service
     */
    private void conectWithService() {
        Intent serviceIntent = new Intent("util.serviceconnector.ECHO_SERVICE");
        serviceIntent.setComponent(new ComponentName("util.serviceconnector.service", "util.serviceconnector.service.EchoService"));
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
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
        conectWithService();
    }

}
