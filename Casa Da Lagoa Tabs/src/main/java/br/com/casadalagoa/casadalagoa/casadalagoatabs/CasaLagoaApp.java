package br.com.casadalagoa.casadalagoa.casadalagoatabs;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by ssu on 13/03/14.
 */
 public class CasaLagoaApp extends Application {

    private static Context context;
    private static ActionBarActivity mActionBarActivity;

        public void onCreate() {
            super.onCreate();
            CasaLagoaApp.context = getApplicationContext();
        }

        private ActionBarActivity mCurrentActivity = null;
        public ActionBarActivity getCurrentActivity(){
            return mCurrentActivity;
        }
        public void setCurrentActivity(ActionBarActivity mCurrentActivity){
            this.mCurrentActivity = mCurrentActivity;
        }

    public synchronized static Context getAppContext() {
        return CasaLagoaApp.context;
    }

    /**
     * setCurrentActivity(null) in onPause() on each activity
     * setCurrentActivity(this) in onResume() on each activity
     *
     */
    public static ActionBarActivity currentActivity() {
        return mActionBarActivity;
    }
 }

