package fr.skalit.websocketnotificationpoc.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import fr.skalit.websocketnotificationpoc.R;
import fr.skalit.websocketnotificationpoc.com.TopicWebApiConstants;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by pascalvincent on 27/05/2016.
 */
public class AlertService extends Service {

    public static final String TAG = AlertService.class.getSimpleName();
    private Socket socket;

    // Notification ID to allow for future updates
    private static final int MY_NOTIFICATION_ID = 1;

    private static final String APP_MODEL = "topic";

    Emitter.Listener onNewAlerts = null;

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

                if (action.equals("START")) {
                    Log.d(TAG, "starting service");
                    initialize();
                } else if (action.equals("SUBSCRIBE")) {
                    Log.d(TAG, "subscribe action");
                    subscribe(intent.getStringExtra("topicId"), intent.getStringExtra("topicName"));
                } else if (action.equals("UNSUBSCRIBE_ALL")) {
                    Log.d(TAG, "unsubscribe action");
                    unsubscribeAll();
                } else {
                    Log.d(TAG, "nothing to do for action : " + action);
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

                    // TODO à faire dans un thread à part
                    // IntentService ?

                    Log.d(TAG, "websocket received alerts event");

                    JSONObject payload = (JSONObject) args[0];
                    String verb;
                    JSONObject data;
                    JSONObject message;
                    String content;
                    try {
                        verb = payload.getString("verb");
                        data = payload.getJSONObject("data");
                        message = data.getJSONObject("message");
                        content = message.getString("content");
                    } catch (JSONException e) {
                        Log.d(TAG, "onNewAlert JSONException : " + e.getMessage());
                        return;
                    }

                    // log
                    Log.d(TAG, "alerts - " + verb + " : " + data);

                    // notify !
                    // Build the Notification
                    Context context = getApplicationContext();
                    // Notification Text Elements
                    CharSequence tickerText = "Topic alert : received a new alert !";
                    CharSequence contentTitle = "Topic alert";
                    CharSequence contentText = content;

                    Notification.Builder notificationBuilder = new Notification.Builder(context)
                            .setTicker(tickerText)
                            .setSmallIcon(R.drawable.ic_stat_alert)
                            .setAutoCancel(true)
                            .setContentTitle(contentTitle)
                            .setContentText(contentText);

                    // Get the NotificationManager
                    NotificationManager mNotificationManager = (NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);

                    // Pass the Notification to the NotificationManager:
                    mNotificationManager.notify(MY_NOTIFICATION_ID,
                            notificationBuilder.build());

                    // TODO cumuler les notifications
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
                    }

                }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(TAG, "websocket connection error");
                    }

                }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(TAG, "websocket timeout");
                    }

                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(TAG, "websocket disconnected");
                    }

                }).on(APP_MODEL, onNewAlerts);

                socket.connect();

            } catch (URISyntaxException e) {
                Log.e(TAG, "socket create : " + e.getMessage());
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void subscribe(String topicId, String topicName) {
        if (topicId == null || topicId.isEmpty()) {
            Log.d(TAG, "no topic id !");
            return;
        }
        if (topicName == null || topicName.isEmpty()) {
            Log.d(TAG, "no topic name !");
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("url", "/" + APP_MODEL + "/" + topicId);
            json.put("method", "GET");
        } catch (JSONException e) {
            Log.e(TAG, "subscribe json error " + e.getMessage());
        }

        if (socket.connected()) {
            Log.d(TAG, "websocket subscribing to " + topicName);
            socket.emit("get", json);
        } else {
            Log.e(TAG, "socket not connected");
        }
    }

    private void unsubscribeAll() {
        // stop listening to topic event
        Log.d(TAG, "unsubscribe to all");
        socket.off(APP_MODEL, onNewAlerts);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroying service");
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
        onNewAlerts = null;
    }
}
