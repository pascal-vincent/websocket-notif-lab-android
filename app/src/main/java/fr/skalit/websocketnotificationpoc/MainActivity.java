package fr.skalit.websocketnotificationpoc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fr.skalit.websocketnotificationpoc.model.Topic;
import fr.skalit.websocketnotificationpoc.service.AlertService;
import fr.skalit.websocketnotificationpoc.service.TopicManager;
import fr.skalit.websocketnotificationpoc.service.TopicService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ListView mTopicListView;
    private ArrayAdapter<String> mAdapter;

    public TopicService topicService;
    public TopicManager topicManager;

    SharedPreferences sharedPreferences;

    AlertServiceLocalBroadCastReceiver mLocalBroadCastReceiver;
    IntentFilter mAlertServiceStatusIntentFilter = new IntentFilter("ALERT_SERVICE_STARTED_BROADCAST");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate ...");

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        mTopicListView = (ListView) findViewById(R.id.list_topic);

        mLocalBroadCastReceiver = new AlertServiceLocalBroadCastReceiver(this);

        // register the broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadCastReceiver,mAlertServiceStatusIntentFilter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart ...");

        // TODO use an intentService for retrofit call
        //initialize the service
        if (topicService == null) {
            topicService = new TopicService();
        }
        topicService.initialize(this);

        if(topicManager == null) {
            topicManager = new TopicManager(sharedPreferences);
        }

        // retrieve receive alert choice
        boolean receiveAlertPref = topicManager.getReceiveAlertPref();
        // set checkbox state
        CheckBox checkBox = (CheckBox) findViewById(R.id.receiveAlertPref);
        assert checkBox != null;
        checkBox.setChecked(receiveAlertPref);

        // start service if needed
        if (receiveAlertPref) {
            startAlertService();
        }

        updateUI();

        // TODO show socket state
        // TODO add a reconnect button if not connected

        // TODO show the last topic message with date and hour

        // TODO show previous message for topic : add a menu for a long press on a topic ; delete, show messages

    }

    private void startAlertService() {
        Log.d(TAG, "starting alert service");
        Intent intent = new Intent(this, AlertService.class);
        intent.setAction("START");
        startService(intent);
    }

    private void stopAlertService() {
        Log.d(TAG, "stopping alert service");
        Intent intent = new Intent(this, AlertService.class);
        intent.setAction("STOP");
        stopService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop ...");

        topicService.release();
        topicService = null;
    }

    // TODO onPause
    // Stop animations or other ongoing actions that could consume CPU.
    // save what should be saved before onStop
    // Release system resources, such as broadcast receivers, handles to sensors
    // or any resources that may affect battery life while your activity is paused and the user does not need them.


    // TODO onResume
    // initialize what was released in onPause
    //

    // TODO onRestart ..


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
                                String topicName = String.valueOf(topicEditText.getText());
                                Log.d(TAG, "Topic to add: " + topicName);
                                // get topic object from remote server and subscribe
                                topicService.checkTopicAndSubscribe(topicName);
                                // TODO ajouter une animation pendant recherche du topic
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
        ArrayList<String> topicList = topicManager.list();

        Log.d(TAG, "Liste des abonnements : " + topicList.toString());

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
        String topicName = String.valueOf(topicTextView.getText());

        topicManager.delete(topicName);

        unsubscribeViaAlertService(topicName);

        updateUI();
    }

    public void checkMessage(View view) {
        topicService.getTopicListByNames();
    }

    public void saveTopicAndSubscribe(Topic topic) {
        if (topic == null) {
            Toast.makeText(this, "Code non trouvé", Toast.LENGTH_SHORT).show();
            return;
        }
        // save topic
        // TODO faire la sauvegarde des topic en dehors du thread UI (async ?)
        topicManager.add(topic.getName());

        updateUI();

        subscribeViaAlertService(topic);

    }

    private void subscribeViaAlertService(Topic topic) {
        // send a subscribe intent to the alert service
        Intent intent = new Intent(this, AlertService.class);
        intent.setAction("SUBSCRIBE");
        intent.putExtra("topicName", topic.getName());
        startService(intent);
    }

    private void unsubscribeViaAlertService(String topicName) {
        Intent intent = new Intent(this, AlertService.class);
        intent.setAction("UNSUBSCRIBE");
        intent.putExtra("topicName", topicName);
        startService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy ...");
    }

    public void onReceiveAlertClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        Log.d(TAG, "receive alert : " + checked);
        // update preference
        topicManager.setReceiveAlertPref(checked);

        // start/stop service
        if (checked) {
            // start service
            startAlertService();
        } else {
            // stop service
            stopAlertService();
        }

    }

    // called when the AlertService is started
    public void restoreSubscription() {
        Log.d(TAG, "restoring subscription");
        // restore subscription
        for(String topicName : topicManager.list()) {
            topicService.checkTopicAndSubscribe(topicName);
        }
    }
}
