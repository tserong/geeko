package org.opensuse.tserong.geeko;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by tserong on 10/21/14.
 */
class GeekoGLRenderer implements GLSurfaceView.Renderer {

    private Triangle mTriangle;
    private Square mSquare;

    // From demos
    private static final String TAG = "GeekoGLRenderer";
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    private float mAngle;

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    // Called once to set up the view's OpenGL ES environment
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        //GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);

        mTriangle = new Triangle();
        mSquare = new Square();
    }

    // Called for each redraw of the view
    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // from SDK demo
        float[] scratch = new float[16];
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        mSquare.draw(mMVPMatrix);

        /*
        // auto rotate (needs RENDERMODE_WHEN_DIRTY unset)
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int)time);
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);
        */
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        mTriangle.draw(scratch);
    }

    // Called if the geometry of the view changes, e.g.: when the screen orientation changes
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // from SDK demo
        float ratio = (float) width / height;
        // Demo had 2 as 3 here, meaning nothing got displayed
        // (see http://stackoverflow.com/questions/20219093/android-opengl-example-gives-blank-screen)
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 2, 7);
    }

    /**
    * Utility method for debugging OpenGL calls. Provide the name of the call
    * just after making it (from SDK demo):
    *
    * <pre>
    * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
    * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
    *
    * If the operation is not successful, the check throws an error.
    *
    * @param glOperation - Name of the OpenGL call to check.
    */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

}
