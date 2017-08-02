package util.serviceconnector.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import util.serviceconnecor.service.IMathService;

/**
 * Sample math service
 */
public class MathService extends Service {

    private IBinder mathServiceImpl = new IMathService.Stub() {

        public int sum(int first, int second){
            return first + second;
        }

    };

    public MathService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mathServiceImpl;
    }
}
