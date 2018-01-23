package gmedia.net.id.pullens.Application;

import android.app.Application;

import gmedia.net.id.pullens.Utils.TypefaceUtil;

/**
 * Created by Shinmaul on 1/22/2018.
 */

public class FontControl extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/segoeui.ttf");
    }
}
