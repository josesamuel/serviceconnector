package util.serviceconnector.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import util.serviceconnector.service.IEchoService;

/**
 * Sample echo service
 */
public class EchoService extends Service {

    private static final String TAG = EchoService.class.getSimpleName();
    private IBinder echoServiceImpl = new IEchoService.Stub() {

        @Override
        public String echo(String aString) throws RemoteException {
            Log.v(TAG, "Echoing " + aString);
            return aString;
        }
    };

    public EchoService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Service Create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return echoServiceImpl;
    }
}
