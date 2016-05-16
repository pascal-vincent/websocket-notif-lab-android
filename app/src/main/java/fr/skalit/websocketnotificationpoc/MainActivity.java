package fr.skalit.websocketnotificationpoc;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView mTopicListView;
    private ArrayAdapter<String> mAdapter;
    private TopicManager mTopicManager;
    private Socket mSocket;
    // Notification ID to allow for future updates
    private static final int MY_NOTIFICATION_ID = 1;

    private Emitter.Listener onNewAlerts = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            Log.d(TAG, "websocket received alerts event");

            MainActivity.this.runOnUiThread(new Runnable() {
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
                    Context context = MainActivity.this.getApplicationContext();
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
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTopicManager = new TopicManager(getPreferences(Context.MODE_PRIVATE));

        setContentView(R.layout.activity_main);

        mTopicListView = (ListView) findViewById(R.id.list_topic);

        try {
            mSocket = IO.socket("http://wsnotiflab-c9lab2os.rhcloud.com:8000/?__sails_io_sdk_version=0.13.6");
            Log.d(TAG, "websocket created ...");

            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.d(TAG, "websocket connected");

                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // restaurer les abonnements

                            // rechercher les topic par nom/code
                            // lister les codes
                            // mTopicManager.list() ...

                            // en GET HTTP REST pour ne pas souscrire
                            // une requête par code ou une requête avec tous les codes ?

                            // gérer le cas où le topic n'existe pas

                            // si existe extraire l'ID
                            // puis faire le subscribe sur le model/id

                            JSONObject json = new JSONObject();
                            try {
                                json.put("url", "/alerts");
                                json.put("method","GET");
                            } catch (JSONException e) {
                                Log.d(TAG, "websocket subscribe json error " + e.getMessage());
                            }

                            Log.d(TAG, "websocket subscribing to alerts");
                            mSocket.emit("get", json);
                        }
                    });
                }

            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener(){

                @Override
                public void call(Object... args) {
                    Log.d(TAG, "websocket connection error");
                }

            }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener(){

                @Override
                public void call(Object... args) {
                    Log.d(TAG, "websocket timeout");
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener(){

                @Override
                public void call(Object... args) {
                    Log.d(TAG, "websocket disconnected");
                }

            }).on("alerts", onNewAlerts);

            mSocket.connect();

        } catch (URISyntaxException e) {
            Log.d(TAG, "websocket connect error : " + e.getMessage());
        }

        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_add_topic:
                Log.d(TAG, "Add a new topic");
                // ajouter un champ de saisie d'un code regate

                final EditText topicEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Ajouter un abonnement")
                        .setMessage("Saisir le code regate")
                        .setView(topicEditText)
                        .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String topic = String.valueOf(topicEditText.getText());
                                Log.d(TAG, "Topic to add: " + topic);
                                // faire la sauvegarde des topic en dehors du thread UI (async ?)
                                mTopicManager.add(topic);
                                updateUI();
                            }
                        })
                        .setNegativeButton("Annuler", null)
                        .create();
                dialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI() {
        ArrayList<String> topicList = mTopicManager.list();

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_topic,
                    R.id.topic_title,
                    topicList);
            mTopicListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(topicList);
            mAdapter.notifyDataSetChanged();
        }

    }

    public void deleteTopic(View view) {
        View parent = (View) view.getParent();
        TextView topicTextView = (TextView) parent.findViewById(R.id.topic_title);
        String topic = String.valueOf(topicTextView.getText());
        mTopicManager.delete(topic);
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("alerts", onNewAlerts);
        // TODO supprimer tous les abonnements
    }


    // afficher une notif quand event sur le topic suivi

    // afficher une notif même si appli éteinte

    // conservation des abonnements aprés A/R app

    // afficher plusieurs notifications à la suite : plusieurs ID de notif

}
