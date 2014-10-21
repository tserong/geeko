package org.opensuse.tserong.geeko;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MainActivity extends ActionBarActivity {

    class GeekoGLRenderer implements GLSurfaceView.Renderer {

        // Called once to set up the view's OpenGL ES environment
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        }

        // Called for each redraw of the view
        public void onDrawFrame(GL10 unused) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }

        // Called if the geometry of the view changes, e.g.: when the screen orientation changes
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }
    }

    class GeekoGLSurfaceView extends GLSurfaceView {
        public GeekoGLSurfaceView(Context context) {
            super(context);
            // Have to call setEGLContextClientVersion() before setRenderer()
            // (see http://stackoverflow.com/questions/13301468/opengles-on-android-illegalstateexception-setrenderer-has-already-been-called)
            setEGLContextClientVersion(2);
            setRenderer(new GeekoGLRenderer());
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
    }

    private GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: can activity_main.xml be deleted now?
        //setContentView(R.layout.activity_main);

        mGLView = new GeekoGLSurfaceView(this);
        setContentView(mGLView);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
