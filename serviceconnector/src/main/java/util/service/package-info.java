/**
 * Field and method binding for connecting with Android remote AIDL services.
 *
 * Replace the boiler plate service connection codes like -
 * <pre><code>
 *
 * public class ActivityWithoutServiceConnector extends Activity {
 *
 *      //remote service interface
 *      private IEchoService mEchoService;
 *
 *      private ServiceConnection serviceConnection=new ServiceConnection(){
 *
 *          public void onServiceConnected(ComponentName componentName,IBinder service){
 *              //get the service
 *              mEchoService=IEchoService.Stub.asInterface(service);
 *          }
 *
 *          public void onServiceDisconnected(ComponentName componentName){
 *              mEchoService=null;
 *          }
 *      };
 *
 *
 *      private void conectWithService(){
 *          Intent serviceIntent=new Intent("util.serviceconnector.ECHO_SERVICE");
 *          serviceIntent.setComponent(new ComponentName("util.serviceconnector.service","util.serviceconnector.service.EchoService"));
 *          startService(serviceIntent);
 *          bindService(serviceIntent,serviceConnection,Context.BIND_AUTO_CREATE);
 *      }
 *
 *      protected void onCreate(Bundle savedInstanceState){
 *          ...
 *          conectWithService();
 *      }
 *
 * }
 *
 * </code></pre>
 *
 * Replace it with {@link util.service.ServiceConnector}
 * <pre><code>
 *
 * public class ActivityWithServiceConnector extends Activity {
 *
 *      {@literal @}ServiceInfo(serviceIntent="com.myintent.MY_SERVICE")
 *      private IEchoService mEchoService;
 *
 *      protected void onCreate(Bundle savedInstanceState){
 *          ...
 *          ServiceConnector.bind(this, this);
 *      }
 * }
 *
 * </code></pre>
 *
 */
package util.service;