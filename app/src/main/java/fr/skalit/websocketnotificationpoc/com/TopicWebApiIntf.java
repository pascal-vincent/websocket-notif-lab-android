package fr.skalit.websocketnotificationpoc.com;

import java.util.List;

import fr.skalit.websocketnotificationpoc.model.Topic;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by pascalvincent on 17/05/2016.
 */
public interface TopicWebApiIntf {

    // get all topics
    @GET("/topic")
    Call<List<Topic>> topicList();

    // get some user by idz
    // using sails waterline orm 'where' filter
    // ex: topic?where={"id":["codeA","codeB"]}
    @GET("/topic")
    Call<List<Topic>> topicListWhere(@Query("where") String inFilter);

    // get topic by name
    @GET("/topic")
    Call<List<Topic>> topicByName(@Query("name") String name);
}
