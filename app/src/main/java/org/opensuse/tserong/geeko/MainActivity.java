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
