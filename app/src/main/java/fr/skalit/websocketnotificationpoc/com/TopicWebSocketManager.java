package fr.skalit.websocketnotificationpoc.com;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.squareup.haha.perflib.Main;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import fr.skalit.websocketnotificationpoc.MainActivity;
import fr.skalit.websocketnotificationpoc.R;
import fr.skalit.websocketnotificationpoc.model.Topic;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by pascalvincent on 21/05/2016.
 */
public class TopicWebSocketManager {
    private final static String TAG = "TopicWebSocketManager";

    private Socket socket;

    // Notification ID to allow for future updates
    private static final int MY_NOTIFICATION_ID = 1;

    MainActivity activity;

    private static final String APP_MODEL = "topic";

    Emitter.Listener onNewAlerts = null;

    public TopicWebSocketManager(final MainActivity activity) {
        this.activity = activity;

        this.onNewAlerts = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                Log.d(TAG, "websocket received alerts event");

                if (activity != null) {

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject payload = (JSONObject) args[0];
                            String verb;
                            JSONObject data;
                            String message;
                            try {
                                verb = payload.getString("verb");
                                data = payload.getJSONObject("data");
                                message = data.getString("message");
                            } catch (JSONException e) {
                                Log.d(TAG, "onNewAlert JSONException : " + e.getMessage());
                                return;
                            }

                            // log
                            Log.d(TAG, "alerts - " + verb + " : " + data);

                            // notify !
                            // Build the Notification
                            if (activity != null) {
                                Context context = activity.getApplicationContext();
                                // Notification Text Elements
                                CharSequence tickerText = "Topic alert : received a new alert !";
                                CharSequence contentTitle = "Topic alert";
                                CharSequence contentText = message;

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
                            } else {
                                Log.e(TAG, "building notification : no activity ...");
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "received alert event but no activity ...");
                }
            }
        };

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


    public void disconnect() {

        socket.disconnect();

        // TODO supprimer tous les abonnements
        // socket.off(APP_MODEL, onNewAlerts);

    }

}
