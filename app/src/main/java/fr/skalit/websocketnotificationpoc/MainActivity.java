package fr.skalit.websocketnotificationpoc;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Messenger;
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

    Boolean receiveAlertDefault = true;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate ...");

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        mTopicListView = (ListView) findViewById(R.id.list_topic);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart ...");

        //initialize the service
        if(topicService == null){
            topicService = new TopicService();
        }
        topicService.initialize(this);

        topicManager = new TopicManager(sharedPreferences);

        // TODO restauration des abonnements

        // retrieve receive alert choice
        boolean receiveAlertPref = topicManager.getReceiveAlertPref();
        // set checkbox state
        CheckBox checkBox = (CheckBox) findViewById(R.id.receiveAlertPref);
        checkBox.setChecked(receiveAlertPref);
        // start service if needed
        if(receiveAlertPref) {
            startAlertService();
        }

        updateUI();

    }

    private void startAlertService() {
        Intent intent = new Intent(this, AlertService.class);
        intent.setAction("START");
        Log.d(TAG, "starting alert service");
        startService(intent);
    }

    private void stopAlertService() {
        Intent intent = new Intent(this, AlertService.class);
        intent.setAction("STOP");
        Log.d(TAG, "stopping alert service");
        stopService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop ...");
        //don't forget to release your service to avoir memory leak
        topicService.release();
        topicService=null;
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
                                String topic = String.valueOf(topicEditText.getText());
                                Log.d(TAG, "Topic to add: " + topic);
                                subscribe(topic);
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
        String topic = String.valueOf(topicTextView.getText());

        topicManager.delete(topic);
        updateUI();
    }

    public void checkMessage(View view) {
        topicService.getTopicListByNames();
    }

    public void subscribe(String topicName) {
        // TODO faire la sauvegarde des topic en dehors du thread UI (async ?)
        topicManager.add(topicName);

        // get topic object from remote server and subscribe
        topicService.getTopicByName(topicName);

        // TODO faire un appel async et chainer avec la souscription par le web socket manager
    }

    public void subscribeViaWS(Topic topic) {
        Intent intent = new Intent(this, AlertService.class);
        intent.setAction("SUBSCRIBE");
        intent.putExtra("topicId", topic.getId());
        intent.putExtra("topicName", topic.getName());
        startService(intent);
        // topicWebSocketManager.subscribe(topic);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy ...");
    }

    // TODO créer un service pour écouter les réceptions de topics
    // le service doit faire ses traitements dans un thread à part


    public void onReceiveAlertClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        Log.d(TAG, "receive alert : " + checked);
        // update preference
        topicManager.setReceiveAlertPref(checked);

        // start/stop service
        if(checked) {
            // start service
            startAlertService();
        } else {
            // stop service
            stopAlertService();
        }

    }


    // afficher plusieurs notifications à la suite : plusieurs ID de notif

}
