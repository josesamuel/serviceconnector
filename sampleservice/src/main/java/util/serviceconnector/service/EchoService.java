package util.serviceconnector.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import util.serviceconnecor.service.IEchoService;

/**
 * Sample echo service
 */
public class EchoService extends Service {

    private IBinder echoServiceImpl = new IEchoService.Stub() {

        @Override
        public String echo(String aString) throws RemoteException {
            return aString;
        }
    };

    public EchoService() {
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
