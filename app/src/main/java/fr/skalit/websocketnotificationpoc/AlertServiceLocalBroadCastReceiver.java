package fr.skalit.websocketnotificationpoc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.squareup.haha.perflib.Main;

/**
 * Created by pascalvincent on 28/05/2016.
 */
public class AlertServiceLocalBroadCastReceiver extends BroadcastReceiver {
    private static final String TAG = AlertServiceLocalBroadCastReceiver.class.getSimpleName();

    MainActivity activity;

    public AlertServiceLocalBroadCastReceiver(MainActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            String action = intent.getAction();
            if(action != null && !action.isEmpty()) {
                if(action.equals("ALERT_SERVICE_STARTED_BROADCAST")) {
                    if(intent.getBooleanExtra("STARTED", false) == true) {
                        activity.restoreSubscription();
                    } else {
                        Log.d(TAG, "Alert service not started");
                    }
                }
            } else {
                Log.d(TAG, "no action");
            }
        } else {
            Log.d(TAG, "no intent");
        }
    }
}
