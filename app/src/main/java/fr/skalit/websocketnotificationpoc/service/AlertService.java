package fr.skalit.websocketnotificationpoc.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import fr.skalit.websocketnotificationpoc.com.TopicWebApiConstants;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by pascalvincent on 27/05/2016.
 */
public class AlertService extends Service {

    private static final String TAG = AlertService.class.getSimpleName();
    private Socket socket;

    // Notification ID to allow for future updates
    private static final int MY_NOTIFICATION_ID = 1;

    private static final String APP_MODEL = "topic";

    private Emitter.Listener onNewAlerts = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "on create");
        // android.os.Debug.waitForDebugger();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "receive start command");

        if (intent != null) {
            String action = intent.getAction();
            if (action != null && !action.isEmpty()) {

                switch (action) {
                    case "START":
                        Log.d(TAG, "starting service");
                        initialize();
                        break;
                    case "STOP":
                        Log.d(TAG, "stopping service");
                        stopService();
                        break;
                    case "SUBSCRIBE":
                        Log.d(TAG, "subscribe action");
                        subscribe(intent.getStringExtra("topicName"));
                        break;
                    case "UNSUBSCRIBE":
                        Log.d(TAG, "unsubscribe action");
                        unsubscribe(intent.getStringExtra("topicName"));
                        break;
                    case "UNSUBSCRIBE_ALL":
                        Log.d(TAG, "unsubscribe all action");
                        unsubscribeAll();
                        break;
                    default:
                        Log.d(TAG, "nothing to do for action : " + action);
                        break;
                }

            } else {
                Log.d(TAG, "no action found !");
            }
        } else {
            Log.d(TAG, "no intent may be restarting");
        }

        return START_STICKY;
    }

    private void initialize() {
        // check if already started
        if (onNewAlerts == null) {

            onNewAlerts = new Emitter.Listener() {
                @Override
                public void call(final Object... args) {

                    Log.d(TAG, "websocket received alerts event");

                    JSONObject payload = (JSONObject) args[0];

                    Intent intent = new Intent(getApplicationContext(), AlertMessageManager.class);
                    intent.setAction("NOTIFY");
                    intent.putExtra("payload", payload.toString());
                    startService(intent);
                }
            };
        }

        if (socket == null) {

            try {

                socket = IO.socket(TopicWebApiConstants.WEBSOCKET_APP_URL);

                Log.d(TAG, "websocket created ...");

                socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(TAG, "websocket connected");
                        sendConnectedBroadcast(true);
                    }

                }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(TAG, "websocket connection error");
                        sendConnectedBroadcast(false);
                    }

                }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(TAG, "websocket timeout");

                        // TODO deal with time out : try to reconnect ?
                    }

                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(TAG, "websocket disconnected");
                        sendConnectedBroadcast(false);
                    }

                }).on(APP_MODEL, onNewAlerts);

                socket.connect();

            } catch (URISyntaxException e) {
                Log.e(TAG, "socket create : " + e.getMessage());
            }
        }

    }

    private void sendConnectedBroadcast(boolean isConnected) {
        Intent intent = new Intent("ALERT_SERVICE_CONNECTED_BROADCAST");
        intent.putExtra("CONNECTED", isConnected);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void subscribe(String topicName) {
        if (topicName == null || topicName.isEmpty()) {
            Log.d(TAG, "no topic name !");
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("url", "/" + APP_MODEL + "?where={\"name\":\"" + topicName + "\"}");
            json.put("method", "GET");
        } catch (JSONException e) {
            Log.e(TAG, "subscribe json error " + e.getMessage());
        }

        if (socket != null && socket.connected()) {
            Log.d(TAG, "websocket subscribing to " + topicName);
            socket.emit("get", json);
        } else {
            Log.e(TAG, "subscribe error : socket not connected");
            // local broadcast the error
            sendConnectedBroadcast(false);
        }
    }

    private void unsubscribe(String topicName) {
        if (topicName == null || topicName.isEmpty()) {
            Log.d(TAG, "no topic name !");
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("url", "/" + APP_MODEL + "/unsubscribe/" + topicName);
            json.put("method", "GET");
        } catch (JSONException e) {
            Log.e(TAG, "subscribe json error " + e.getMessage());
        }

        if (socket != null && socket.connected()) {
            Log.d(TAG, "websocket unsubscribing to " + topicName);
            socket.emit("get", json);
        } else {
            Log.e(TAG, "unsubscribe error : socket not connected");
            // local broadcast the error
            sendConnectedBroadcast(false);
        }
    }

    private void unsubscribeAll() {
        // stop listening to all topic event
        // TODO unsubscribe each topic
        Log.d(TAG, "unsubscribe to all");
        socket.off(APP_MODEL, onNewAlerts);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroying service");
        stopService();
    }

    private void stopService() {
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
        onNewAlerts = null;
    }
}
