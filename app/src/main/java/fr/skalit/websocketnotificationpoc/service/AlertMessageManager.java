package fr.skalit.websocketnotificationpoc.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import fr.skalit.websocketnotificationpoc.R;

/**
 * Created by pascalvincent on 02/06/2016.
 */
public class AlertMessageManager extends IntentService {

    private static final String TAG = AlertMessageManager.class.getSimpleName();

    private static final int MY_NOTIFICATION_ID = 1;

    public AlertMessageManager() {
        super("AlertMessageManagerWsNotifPoc");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();

        if (intent != null) {
            if (action != null && !action.isEmpty()) {
                if (action.equals("NOTIFY")) {
                    Log.d(TAG, "notitying");
                    String sPayload = intent.getStringExtra("payload");
                    if(sPayload != null && !sPayload.isEmpty()) {
                        try {

                            JSONObject payload = new JSONObject(sPayload);
                            String alertMessage = getAlertMessage(payload);
                            notifyAlert(alertMessage);

                        } catch (JSONException e) {
                            Log.e(TAG, "json payload error " + e.getMessage());
                        }

                    }

                } else {
                    Log.d(TAG, "nothing to do for action : " + action);
                }

            } else {
                Log.d(TAG, "no action found !");
            }
        } else {
            Log.d(TAG, "no intent may be restarting");
        }
    }

    private String getAlertMessage(JSONObject json) {

        JSONObject payload = json;
        String verb;
        JSONObject data;
        JSONObject message;
        String content = null;
        try {
            verb = payload.getString("verb");
            data = payload.getJSONObject("data");
            message = data.getJSONObject("message");
            content = message.getString("content");

            // log
            Log.d(TAG, "alerts - " + verb + " : " + data);

        } catch (JSONException e) {
            Log.d(TAG, "onNewAlert JSONException : " + e.getMessage());
        }

        return content;
    }

    private void notifyAlert(String content) {
        // notify !
        // Build the Notification
        Context context = getApplicationContext();
        // Notification Text Elements
        CharSequence tickerText = "Topic alert : received a new alert !";
        CharSequence contentTitle = "Topic alert";

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setTicker(tickerText)
                .setSmallIcon(R.drawable.ic_stat_alert)
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(content);

        // Get the NotificationManager
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Pass the Notification to the NotificationManager:
        mNotificationManager.notify(MY_NOTIFICATION_ID,
                notificationBuilder.build());

        // TODO cumuler les notifications
    }
}
