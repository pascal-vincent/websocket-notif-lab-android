package fr.skalit.websocketnotificationpoc;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by pascalvincent on 21/05/2016.
 */
public class WsNotifApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
