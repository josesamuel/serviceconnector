ServiceConnector
============


Field and method binding for connecting with Android remote AIDL services.


Replace the boiler plate service connection codes like - 

```java
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
        setContentView(R.layout.activity);
        conectWithService();
    }

}
```



Replace that with

```java
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
        setContentView(R.layout.activity);
        ServiceConnector.bind(this, this);
    }
}
```



Download
--------

```groovy
dependencies {
  compile 'com.josesamuel:serviceconnector:1.0.0’
}
```

License
-------

    Copyright 2017 Joseph Samuel

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


