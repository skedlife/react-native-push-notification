package com.dieam.reactnativepushnotification.modules;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactHost;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationListenerService extends FirebaseMessagingService {

    private RNReceivedMessageHandler mMessageReceivedHandler;
    private FirebaseMessagingService mFirebaseServiceDelegate;

    public RNPushNotificationListenerService() {
        super();
        this.mMessageReceivedHandler = new RNReceivedMessageHandler(this);
    }

    public RNPushNotificationListenerService(FirebaseMessagingService delegate) {
        super();
        this.mFirebaseServiceDelegate = delegate;
        this.mMessageReceivedHandler = new RNReceivedMessageHandler(delegate);
    }

    @Override
    public void onNewToken(String token) {
        final String deviceToken = token;
        final FirebaseMessagingService serviceRef = (this.mFirebaseServiceDelegate == null) ? this : this.mFirebaseServiceDelegate;
        Log.d(LOG_TAG, "Refreshed token: " + deviceToken);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                ReactHost reactHost = ((ReactApplication) serviceRef.getApplication()).getReactHost();
                ReactContext context = reactHost.getCurrentReactContext();
                if (context != null) {
                    handleNewToken((ReactApplicationContext) context, deviceToken);
                } else {
                    Log.w(LOG_TAG, "ReactContext not ready, token registration may be delayed");
                }
            }
        });
    }

    private void handleNewToken(ReactApplicationContext context, String token) {
        RNPushNotificationJsDelivery jsDelivery = new RNPushNotificationJsDelivery(context);

        WritableMap params = Arguments.createMap();
        params.putString("deviceToken", token);
        jsDelivery.sendEvent("remoteNotificationsRegistered", params);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        mMessageReceivedHandler.handleReceivedMessage(message);
    }
}
