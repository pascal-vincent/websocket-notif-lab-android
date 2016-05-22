package fr.skalit.websocketnotificationpoc.service;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import fr.skalit.websocketnotificationpoc.MainActivity;
import fr.skalit.websocketnotificationpoc.com.TopicWebApiClientBuilder;
import fr.skalit.websocketnotificationpoc.com.TopicWebApiIntf;
import fr.skalit.websocketnotificationpoc.com.TopicWebSocketManager;
import fr.skalit.websocketnotificationpoc.model.Topic;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by pascalvincent on 21/05/2016.
 */
public class TopicService {

    private static final String TAG = "TopicService";

    private MainActivity activity;

    private TopicWebApiIntf topicWebApiService;
    private TopicWebSocketManager topicWebSocketManager;

    private Call<List<Topic>> getTopicListCall = null;

    /**
     * To be call when the activity go in its onStart method
     */
    public void initialize(MainActivity activity) {
        this.activity = activity;

        topicWebApiService = TopicWebApiClientBuilder.getSimpleClient();
        topicWebSocketManager = new TopicWebSocketManager(activity);
    }


    /**
     * To be call when the activity go in its onStop method
     */
    public void release() {
        activity = null;
        getTopicListCall.cancel();
        topicWebSocketManager.disconnect();
    }

    /**
     * Return the topic list from remote server
     */
    public void getTopicList() {

        // create a specific request : http://domain.com/topic?where={"name":["codeA","codeB","codeC]}
        String whereParam = "{\"name\":" + this.activity.topicManager.listAsJsonArray() + "}";

        getTopicListCall = topicWebApiService.topicListWhere(whereParam);

        getTopicListCall.enqueue(new Callback<List<Topic>>() {

            @Override
            public void onResponse(Call<List<Topic>> call, Response<List<Topic>> response) {

                Log.d(TAG, "getTopicListCall : response is back");

                if (response.isSuccessful()) {
                    // request successful (status code 200, 201)
                    List<Topic> result = response.body();
                    if(result != null) {
                        for(Topic topic : result) {
                            Log.d(TAG, "getTopicListCall : " + topic.toString());
                        }
                    }
                } else {
                    //request not successful (like 400,401,403 etc)
                    Log.d(TAG, "getTopicListCall - response code : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Topic>> call, Throwable t) {
                Log.e(TAG, "getTopicListCall error : " + t.getMessage());
            }

        });
    }

    private void subscribe(Topic topic) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("url", "/topic/" + topic.getId());
        json.put("method", "GET");

        Log.d(TAG, "websocket subscribing to " + topic.getName());
        // mSocket.emit("get", json);
    }


    // TODO unsubscribe

}
