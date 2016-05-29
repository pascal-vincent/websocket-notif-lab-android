package fr.skalit.websocketnotificationpoc.com;

import fr.skalit.websocketnotificationpoc.com.TopicWebApiConstants;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Created by pascalvincent on 21/05/2016.
 */
public class TopicWebApiClientBuilder {

    private static final String BASE_URL = TopicWebApiConstants.HTTP_APP_URL;

    /***********************************************************
     * Simple Retrofit WebServer
     **********************************************************/

    public static TopicWebApiIntf getSimpleClient() {
        //Using Default HttpClient
        Retrofit ra = new Retrofit.Builder()
                //you need to add your root url
                .baseUrl(BASE_URL)
                //You need to add a converter if you want your Json to be automagicly convert into the object
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
        return ra.create(TopicWebApiIntf.class);
    }

}
