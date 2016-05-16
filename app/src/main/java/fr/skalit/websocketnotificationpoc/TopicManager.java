package fr.skalit.websocketnotificationpoc;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pascalvincent on 12/05/2016.
 */
public class TopicManager {
    private static final String TAG = "TopicManager";
    private static final String TOPICS_KEY = "topics";

    private SharedPreferences sharedPreferences;

    public TopicManager(SharedPreferences prefs) {
        this.sharedPreferences = prefs;
    }

    public ArrayList<String> list() {
        ArrayList<String> topicList = new ArrayList<>();
        //Retrieve the values
        Set<String> topicSet = getSet();

        if (topicSet != null) {
            // lister les codes regates existant
            topicList.addAll(topicSet);
        } else {
            topicList.add("no topic found");
        }
        return topicList;
    }

    // TODO ajouter contrainte sur format du topic
    public void add(String topic) {
        //Retrieve the values
        Set<String> topicSet = getSet();

        if (topicSet == null) {
            // creation d'une nouvelle liste
            // stocker une liste de code dans une propriété
            topicSet = new HashSet<String>();
        }
        // on ajoute à la liste existante
        topicSet.add(topic);

        addSet(topicSet);
    }

    private Set<String> getSet() {
        return sharedPreferences.getStringSet(TOPICS_KEY, null);
    }

    private void addSet(Set<String> topicSet) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(TOPICS_KEY, topicSet);
        editor.commit();
    }

    public void delete(String topic) {
        Set<String> topicSet = getSet();
        topicSet.remove(topic);
        addSet(topicSet);
    }
}
