package fr.skalit.websocketnotificationpoc.service;

import android.util.Log;

import java.util.List;

import fr.skalit.websocketnotificationpoc.MainActivity;
import fr.skalit.websocketnotificationpoc.com.TopicWebApiClientBuilder;
import fr.skalit.websocketnotificationpoc.com.TopicWebApiIntf;
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


    private Call<List<Topic>> getTopicListCall = null;
    private Call<List<Topic>> getTopicByNameCall = null;

    /**
     * To be call when the activity go in its onStart method
     */
    public void initialize(MainActivity activity) {
        this.activity = activity;
        topicWebApiService = TopicWebApiClientBuilder.getSimpleClient();
    }


    /**
     * To be call when the activity go in its onStop method
     */
    public void release() {
        activity = null;
        if (getTopicListCall != null) {
            getTopicListCall.cancel();
        }
        if (getTopicByNameCall != null) {
            getTopicByNameCall.cancel();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    /**
     * Return the topic list from remote server
     */
    public void getTopicListByNames() {

        // create a specific request : http://domain.com/topic?where={"name":["codeA","codeB","codeC]}
        String whereParam = "{\"name\":" + this.activity.topicManager.listAsJsonArray() + "}";

        getTopicListCall = topicWebApiService.topicListWhere(whereParam);

        getTopicListCall.enqueue(new Callback<List<Topic>>() {

            @Override
            public void onResponse(Call<List<Topic>> call, Response<List<Topic>> response) {

                Log.d(TAG, "getTopicListByNames : response is back");

                if (response.isSuccessful()) {
                    // request successful (status code 200, 201)
                    List<Topic> result = response.body();
                    if (result != null && !result.isEmpty()) {
                        for (Topic topic : result) {
                            Log.d(TAG, "getTopicListByNames : " + topic.toString());
                        }
                    } else {
                        Log.d(TAG, "getTopicListByNames : empty response result");
                    }
                } else {
                    //request not successful (like 400,401,403 etc)
                    Log.d(TAG, "getTopicListByNames - response code : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Topic>> call, Throwable t) {
                Log.e(TAG, "getTopicListByNames error : " + t.getMessage());
            }

        });
    }

    public void checkTopicAndSubscribe(String name) {

        getTopicByNameCall = topicWebApiService.topicByName(name);

        getTopicByNameCall.enqueue(new Callback<List<Topic>>() {

            @Override
            public void onResponse(Call<List<Topic>> call, Response<List<Topic>> response) {

                Log.d(TAG, "getTopicListCall : response is back");

                Topic topic = null;

                if (response.isSuccessful()) {
                    // request successful (status code 200, 201)
                    List<Topic> topicList = response.body();
                    if (topicList != null && !topicList.isEmpty()) {
                        topic = topicList.get(0);
                        Log.d(TAG, "checkTopicAndSubscribe : " + topic.toString());
                    } else {
                        Log.d(TAG, "empty result response");
                    }
                } else {
                    //request not successful (like 400,401,403 etc)
                    Log.d(TAG, "checkTopicAndSubscribe - response code : " + response.code());
                }

                activity.saveTopicAndSubscribe(topic);
            }

            @Override
            public void onFailure(Call<List<Topic>> call, Throwable t) {
                Log.e(TAG, "checkTopicAndSubscribe error : " + t.getMessage());
                activity.saveTopicAndSubscribe(null);
            }

        });
    }
}
