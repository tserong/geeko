package org.opensuse.tserong.geeko;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private GeekoView mGeekoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGeekoView = new GeekoView(this);
        setContentView(mGeekoView);
        // TODO: Somehow it stops drawing if the screen blanks (i.e. it doesn't start again
        // afer you unblank the screen)
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGeekoView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGeekoView.onResume();
    }

}
