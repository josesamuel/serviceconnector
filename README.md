ServiceConnector
============


Field and method binding for connecting with Android remote AIDL (or [**Remoter**](https://josesamuel.github.io/remoter/)) services.


Replace the boiler plate service connection codes like - 

```java
public class ActivityWithoutServiceConnector extends Activity {

    //remote service interface
    private IEchoService mEchoService;

    /**
     * Service connection that gets call back when service is connected
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            //get the service 
            mEchoService = IEchoService.Stub.asInterface(service);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        conectWithService();
    }

}
```



Replace that with

```java
public class ActivityWithServiceConnector extends Activity {

    @ServiceInfo(serviceIntent = "util.serviceconnector.ECHO_SERVICE")
    private IEchoService mEchoService;

    @ServiceConnectionCallback
    public void onServiceConnectionChanged(String serviceIntent, boolean connected) throws RemoteException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        ServiceConnector.bind(this, this);
    }
}
```



Getting ServiceConnector
--------

```groovy
dependencies {
  compile 'com.josesamuel:serviceconnector:1.0.6'
}
```

Java Doc
--------
<a href="https://josesamuel.github.io/serviceconnector/javadoc/">ServiceConnector Java Doc</a>


License
-------

    Copyright 2018 Joseph Samuel

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


