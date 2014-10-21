package org.opensuse.tserong.geeko;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by tserong on 10/21/14.
 */
public class GeekoView extends SurfaceView implements SurfaceHolder.Callback {

    // Basic thread logic lifted from LunarLander sample
    class GeekoThread extends Thread {

        private SurfaceHolder mSurfaceHolder;

        private int mCanvasWidth = -1;
        private int mCanvasHeight = -1;

        private boolean mRun = false;
        private final Object mRunLock = new Object();

        private Bitmap mBitmap;

        private int mPoint = 0;
        float mPoints[] = new float[128];

        public GeekoThread(SurfaceHolder holder, Context context) {
            mSurfaceHolder = holder;
            // TODO: do we need context?
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;
                mBitmap = Bitmap.createBitmap(mCanvasWidth, mCanvasHeight, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(mBitmap);
                c.drawARGB(255, 155, 186, 37);
                mPoint = 0;
            }
        }

        public void setRunning(boolean b) {
            synchronized (mRunLock) {
                mRun = b;
            }
        }



        private void doDraw(Canvas canvas) {
            Canvas c = new Canvas(mBitmap);
/*
            //long t = SystemClock.uptimeMillis();
            //if ((t - mTime) > 500) {        // TODO: does that want Math.abs()?
                Paint p = new Paint();
                p.setARGB(255, 255, 255, 255);
                p.setStrokeWidth(10);   // ??
                c.drawPoint(x, y, p);
                x += 5;
                y += 5;
            //    t = mTime;
            //}
*/
            if (mPoint == 0) {
                Point p0 = new Point((int)(Math.random() * mCanvasWidth), mCanvasHeight + 10);
                Point p1 = new Point((int)(Math.random() * mCanvasWidth), (int)(Math.random() * mCanvasHeight / 2) + mCanvasHeight / 4);
                Point p2 = new Point((int)(Math.random() * mCanvasWidth), (int)(Math.random() * mCanvasHeight / 2) + mCanvasHeight / 4);
                Point p3 = new Point((int)(Math.random() * mCanvasWidth), (int)(Math.random() * mCanvasHeight / 2));

                // Thanks http://www.fractalnet.org/CAD/curve/BezierCurve.pdf, good to see this is still online ;)
                for (int i = 0; i < 64; i++) {
                    float mu = (float) i / 64;
                    float mum1, mum13, mu3;
                    mum1 = 1 - mu;
                    mum13 = mum1 * mum1 * mum1;
                    mu3 = mu * mu * mu;

                    mPoints[i * 2] = mum13 * p0.x + 3 * mu * mum1 * mum1 * p1.x + 3 * mu * mu * mum1 * p2.x + mu3 * p3.x;
                    mPoints[i * 2 + 1] = mum13 * p0.y + 3 * mu * mum1 * mum1 * p1.y + 3 * mu * mu * mum1 * p2.y + mu3 * p3.y;
                }
            }

            Paint p = new Paint();
            p.setARGB(255, 255, 255, 255);
            p.setStrokeWidth(Math.min(20, 63 - mPoint));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setAntiAlias(true);

            c.drawLines(mPoints, mPoint * 2, 4, p);
            mPoint++;
            if (mPoint >= 63) {
                mPoint = 0;
            }

            canvas.drawBitmap(mBitmap, 0, 0, null);
        }

        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        // if running, do stuff
                        synchronized(mRunLock) {
                            if (mRun) {
                                doDraw(c);
                            }
                        }
                    }
                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
                try {
                    // Is this sane?
                    sleep(100, 0);
                } catch (InterruptedException e) {
                    // Don't care
                }
            }
        }
    }

    private GeekoThread mThread;

    public GeekoView(Context context) {
        super(context);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        mThread = new GeekoThread(holder, context);
    }

    public void onPause() {
        mThread.setRunning(false);
    }

    public void onResume() {
        mThread.setRunning(true);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mThread.setRunning(true);
        mThread.start();
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mThread.setSurfaceSize(width, height);
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        mThread.setRunning(false);
        // TODO: can this ever lock up indefinitely?
        while (retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
}
