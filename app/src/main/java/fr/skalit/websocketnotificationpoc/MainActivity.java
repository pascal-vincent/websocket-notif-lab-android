package fr.skalit.websocketnotificationpoc;

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

import java.util.ArrayList;

import fr.skalit.websocketnotificationpoc.service.TopicManager;
import fr.skalit.websocketnotificationpoc.service.TopicService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView mTopicListView;
    private ArrayAdapter<String> mAdapter;
    private TopicService topicService;
    public TopicManager topicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate ...");
        topicManager = new TopicManager(getPreferences(Context.MODE_PRIVATE));

        setContentView(R.layout.activity_main);

        mTopicListView = (ListView) findViewById(R.id.list_topic);

        updateUI();
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
        topicService.getTopicList();

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop ...");
        //don't forget to release your service to avoir memory leak
        topicService.release();
        topicService=null;
    }

    // TODO onResume, onPause, onRestart ..


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
                                // TODO faire la sauvegarde des topic en dehors du thread UI (async ?)
                                topicManager.add(topic);
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
        topicService.getTopicList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy ...");
    }

    // afficher une notif même si appli éteinte
    // service qui se réveille et répond sur un broadcast

    // conservation des abonnements aprés A/R app

    // afficher plusieurs notifications à la suite : plusieurs ID de notif

}
