package util.service;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import util.service.annotation.ServiceConnectionCallback;

import static util.service.ServiceIntents.INTENT_ECHO_SERVICE;
import static util.service.ServiceIntents.INTENT_MATH_SERVICE;
import static util.service.ServiceIntents.INTENT_TEST_ACTIVITY;

/**
 * Tests the service connector api
 */
public class ServiceConnectorTest {

    private static final String TAG = ServiceConnectorTest.class.getSimpleName();
    private int callBackCounter;


    @Rule
    public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule<TestActivity>(TestActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            return new Intent(INTENT_TEST_ACTIVITY);
        }
    };

    @Before
    public void setup() {
        callBackCounter = 0;
    }

    @Test(timeout = 10000)
    public void testServiceConnection() throws InterruptedException {

        SingleTestTarget testTarget = new SingleTestTarget() {
            @Override
            public void onServiceCallback(String serviceIntent, boolean connected) {
                super.onServiceCallback(serviceIntent, connected);
                callBackCounter++;
            }
        };
        ServiceConnector.setEnableDebug(true);
        ServiceConnector.bind(testTarget, mActivityRule.getActivity());

        ServiceConnector.waitForAllConnected(0);

        Assert.assertTrue("Expected service field set", testTarget.isServiceSet());
        Assert.assertTrue("Expected all service to be connected", ServiceConnector.isAllConnected());
        Assert.assertTrue("Expected  service to be connected", ServiceConnector.isConnected(INTENT_ECHO_SERVICE));
        Assert.assertEquals("Expected  1 calback", 1, callBackCounter);

        ServiceConnector.unbind(testTarget);
    }

    @Test(timeout = 10000)
    public void testMultipleBinds() throws InterruptedException {

        SingleTestTarget testTarget = new SingleTestTarget() {
            @Override
            public void onServiceCallback(String serviceIntent, boolean connected) {
                super.onServiceCallback(serviceIntent, connected);
                Log.v(TAG, "Callback SingleTestTarget " + serviceIntent + connected);
                callBackCounter++;
            }
        };
        ServiceConnector.setEnableDebug(true);
        ServiceConnector.bind(testTarget, mActivityRule.getActivity());

        ServiceConnector.waitForAllConnected(0);
        Log.v(TAG, "All Connected");

        Assert.assertTrue("Expected service field set", testTarget.isServiceSet());
        Assert.assertTrue("Expected all service to be connected", ServiceConnector.isAllConnected());
        Assert.assertTrue("Expected  service to be connected", ServiceConnector.isConnected(INTENT_ECHO_SERVICE));
        Assert.assertFalse("Expected  service not to be connected", ServiceConnector.isConnected(INTENT_MATH_SERVICE));
        Assert.assertEquals("Expected  1 calback", 1, callBackCounter);


        MultipleTestTarget multipleTestTarget = new MultipleTestTarget() {
            @Override
            public void onServiceCallback(String serviceIntent, boolean connected) {
                super.onServiceCallback(serviceIntent, connected);
                Log.v(TAG, "Callback MultipleTestTarget.onServiceCallback " + serviceIntent + connected);
                callBackCounter++;
            }

            @ServiceConnectionCallback
            public void anotherCallBack(String serviceIntent, boolean connected) {
                Log.v(TAG, "Callback MultipleTestTarget.anotherCallback " + serviceIntent + connected);
                callBackCounter++;
            }

        };
        ServiceConnector.bind(multipleTestTarget, mActivityRule.getActivity());

        ServiceConnector.waitForAllConnected(0);
        Log.v(TAG, "All Connected");

        Assert.assertTrue("Expected service field set", multipleTestTarget.isServiceSet());
        Assert.assertTrue("Expected all service to be connected", ServiceConnector.isAllConnected());
        Assert.assertTrue("Expected  service to be connected", ServiceConnector.isConnected(INTENT_ECHO_SERVICE));
        Assert.assertTrue("Expected  service to be connected", ServiceConnector.isConnected(INTENT_MATH_SERVICE));
        Assert.assertEquals("Expected  1 calback", 6, callBackCounter);

        //unbind multitarget. All its field should get unset
        ServiceConnector.unbind(multipleTestTarget);

        //testTarget is still bound
        Assert.assertTrue("Expected service field  set", testTarget.isServiceSet());
        Assert.assertFalse("Expected service field not set", multipleTestTarget.isServiceSet());
        Assert.assertTrue("Expected all service to be connected", ServiceConnector.isAllConnected());
        Assert.assertTrue("Expected  service to be connected", ServiceConnector.isConnected(INTENT_ECHO_SERVICE));
        Assert.assertFalse("Expected  service not to be connected", ServiceConnector.isConnected(INTENT_MATH_SERVICE));

        ServiceConnector.unbind(testTarget);

        Assert.assertFalse("Expected service field  not set", testTarget.isServiceSet());
        Assert.assertFalse("Expected service field not set", multipleTestTarget.isServiceSet());
        Assert.assertFalse("Expected  service not to be connected", ServiceConnector.isConnected(INTENT_ECHO_SERVICE));
        Assert.assertFalse("Expected  service not to be connected", ServiceConnector.isConnected(INTENT_MATH_SERVICE));

    }

}
