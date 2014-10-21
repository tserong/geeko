package org.opensuse.tserong.geeko;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by tserong on 10/21/14.
 */
class GeekoGLSurfaceView extends GLSurfaceView {

    private final GeekoGLRenderer mRenderer;

    public GeekoGLSurfaceView(Context context) {
        super(context);
        // Have to call setEGLContextClientVersion() before setRenderer()
        // (see http://stackoverflow.com/questions/13301468/opengles-on-android-illegalstateexception-setrenderer-has-already-been-called)
        setEGLContextClientVersion(2);
        mRenderer = new GeekoGLRenderer();
        setRenderer(mRenderer);
        // Don't set this to have it continuously draw
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    // From SDK demo
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                if (y > getHeight() / 2) {
                    dx = dx * -1;
                }
                if (x < getWidth() / 2) {
                    dy = dy * -1;
                }

                mRenderer.setAngle(mRenderer.getAngle() + ((dx + dy) * TOUCH_SCALE_FACTOR));
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}
