package fr.skalit.websocketnotificationpoc.com;

/**
 * Created by pascalvincent on 21/05/2016.
 */
class TopicWebApiConstants {

    private static final String APP_DOMAIN_URL = "wsnotiflab-c9lab2os.rhcloud.com";
    private static final int APP_WEBSOCKET_PORT = 8000;
    private static final String APP_WES_PARAMS = "__sails_io_sdk_version=0.13.6";

    public static final String HTTP_APP_URL = "http://"+APP_DOMAIN_URL;

    public static final String WEBSOCKET_APP_URL = HTTP_APP_URL + ":" + APP_WEBSOCKET_PORT + "?" + APP_WES_PARAMS;

}
