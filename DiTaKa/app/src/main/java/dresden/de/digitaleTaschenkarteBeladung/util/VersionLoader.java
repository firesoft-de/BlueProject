package dresden.de.digitaleTaschenkarteBeladung.util;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class VersionLoader extends AsyncTaskLoader<Integer> {

    private static final String LOG_TAG = "VersionLoader_LOG";

    private String url;
    Context context;

    public VersionLoader(Context context, String url) {
        super(context);
        this.context = context;
        this.url = url;
    }

    @Override
    public Integer loadInBackground() {

        Log.d(LOG_TAG,"Start");

        //TODO: Pausieren verbessern
//        for (int i = 0; i < 20000000; i++) {
//            int x = 1+1;
//        }

        Log.d(LOG_TAG,"End");

        return Util_Http.checkVersion(url);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }



}