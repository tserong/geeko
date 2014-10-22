package org.opensuse.tserong.geeko;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by tserong on 10/21/14.
 */
public class GeekoView extends SurfaceView implements SurfaceHolder.Callback {

    // Basic thread logic lifted from LunarLander sample
    class GeekoThread extends Thread {

        private Context mContext;
        private SurfaceHolder mSurfaceHolder;

        private int mCanvasWidth = -1;
        private int mCanvasHeight = -1;

        private boolean mRun = false;
        private final Object mRunLock = new Object();

        private Bitmap mBitmap;

        private Paint mPaint = new Paint();
        private int mPoint = 0;
        float mPoints[] = new float[128];

        private Point mFirstBranch = new Point(0, 0);
        private Point mSecondBranch = new Point(0, 0);

        private int mBranch = 0;

        public GeekoThread(SurfaceHolder holder, Context context) {
            mSurfaceHolder = holder;
            mContext = context;
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;
                mBitmap = Bitmap.createBitmap(mCanvasWidth, mCanvasHeight, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(mBitmap);
                c.drawARGB(255, 70, 69, 71); // Dark Grey
                mPoint = 0;
            }
        }

        public void setRunning(boolean b) {
            synchronized (mRunLock) {
                mRun = b;
            }
        }

        private Point[] generateTrunk() {
            Point[] p = new Point[4];
            // TODO: Fix hard-coded 10
            p[0] = new Point((int)(Math.random() * mCanvasWidth), mCanvasHeight + 10);
            p[3] = new Point((int)(Math.random() * mCanvasWidth), (int)(Math.random() * mCanvasHeight / 2));
            if (Math.random() > 0.5) {
                int rightMostPoint = Math.max(p[0].x, p[3].x);
                p[1] = new Point(
                    (int)(Math.random() * mCanvasWidth - rightMostPoint) + rightMostPoint,
                    (int)(Math.random() * mCanvasHeight / 2) + mCanvasHeight / 4);
                p[2] = new Point(
                    (int)(Math.random() * mCanvasWidth - rightMostPoint) + rightMostPoint,
                    (int)(Math.random() * mCanvasHeight / 4));
            } else {
                int leftMostPoint = Math.min(p[0].x, p[3].x);
                p[1] = new Point(
                        (int)(Math.random() * leftMostPoint),
                        (int)(Math.random() * mCanvasHeight / 2) + mCanvasHeight / 4);
                p[2] = new Point(
                        (int)(Math.random() * leftMostPoint),
                        (int)(Math.random() * mCanvasHeight / 4));
            }
            return p;
        }

        // Scale is what to divide the screen size by, i.e.
        // use 4 to get a quarter of the screen, 8 to get an eighth.
        // TODO: Want these to curl around more like the wallpaper
        private Point[] generateBranch(Point start, int scale) {
            Point[] p = new Point[4];
            p[0] = new Point(start);
            if (Math.random() > 0.5) {
                p[1] = new Point(
                    p[0].x + (int)(Math.random() * mCanvasWidth / scale),
                    p[0].y - mCanvasHeight / scale + (int)(Math.random() * mCanvasHeight / scale));
                p[2] = new Point(
                    p[0].x + mCanvasWidth / scale + (int)(Math.random() * mCanvasWidth / scale),
                    p[0].y - mCanvasHeight / scale + (int)(Math.random() * mCanvasHeight / scale));
                p[3] = new Point(
                    p[0].x + mCanvasWidth / scale + (int)(Math.random() * mCanvasWidth / scale),
                    p[0].y + (int)(Math.random() * mCanvasHeight / scale));
            } else {
                p[1] = new Point(
                    p[0].x - (int)(Math.random() * mCanvasWidth / scale),
                    p[0].y - mCanvasHeight / scale + (int)(Math.random() * mCanvasHeight / scale));
                p[2] = new Point(
                    p[0].x - mCanvasWidth / scale - (int)(Math.random() * mCanvasWidth / scale),
                    p[0].y - mCanvasHeight / scale + (int)(Math.random() * mCanvasHeight / scale));
                p[3] = new Point(
                    p[0].x - mCanvasWidth / scale - (int)(Math.random() * mCanvasWidth / scale),
                    p[0].y + (int)(Math.random() * mCanvasHeight / scale));
            }
            return p;
        }

        private void doDraw(Canvas canvas) {
            Canvas c = new Canvas(mBitmap);

            if (mPoint == 0) {
                Point[] bezier;
                switch (mBranch) {
                    case 1:
                        bezier = generateBranch(mFirstBranch, 4);
                        break;
                    case 2:
                        bezier = generateBranch(mSecondBranch, 8);
                        break;
                    default:
                        bezier = generateTrunk();

                        int colors[] = new int[]{
                                0xff7ac142,     // SUSE Green
                                0xffd4df4c,     // Brigt Green
                                0xff00a54f,     // Medium Green
                                0xff00843e      // Dark Green
                        };

                        mPaint.setColor(colors[(int)(Math.random() * colors.length)]);

                        mFirstBranch.set(0, 0);
                        mSecondBranch.set(0, 0);
                }
                // Thanks http://www.fractalnet.org/CAD/curve/BezierCurve.pdf, good to see this is still online ;)
                for (int i = 0; i < 64; i++) {
                    float mu = (float) i / 64;
                    float mum1, mum13, mu3;
                    mum1 = 1 - mu;
                    mum13 = mum1 * mum1 * mum1;
                    mu3 = mu * mu * mu;

                    mPoints[i * 2] = mum13 * bezier[0].x + 3 * mu * mum1 * mum1 * bezier[1].x + 3 * mu * mu * mum1 * bezier[2].x + mu3 * bezier[3].x;
                    mPoints[i * 2 + 1] = mum13 * bezier[0].y + 3 * mu * mum1 * mum1 * bezier[1].y + 3 * mu * mu * mum1 * bezier[2].y + mu3 * bezier[3].y;

                    if (i == 16 && mBranch == 0) {
                        if (Math.random() > 0.3) {
                            mFirstBranch.set((int)mPoints[i * 2], (int)mPoints[i * 2 + 1]);
                        }
                    }
                    if (i == 32 && mBranch == 0) {
                        if (Math.random() > 0.6) {
                            mSecondBranch.set((int)mPoints[i * 2], (int)mPoints[i * 2 + 1]);
                        }
                    }
                }

            }

            //Paint p = new Paint();
            //p.setARGB(255, 255, 255, 255);
            if (mBranch == 1) {
                mPaint.setStrokeWidth(Math.min(20, 63 - mPoint));
            } else if (mBranch == 2) {
                mPaint.setStrokeWidth(Math.min(15, 63 - mPoint));
            } else {
                mPaint.setStrokeWidth(Math.min(40, 63 - mPoint));
            }
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setAntiAlias(true);

            c.drawLines(mPoints, mPoint * 2, 4, mPaint);
            mPoint++;
            if (mPoint >= 63) {
                if (mBranch == 0) {

                    if (Math.random() > 1.0) {  // >1.0 disables geekos
                        Bitmap geeko = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.geeko);
                        // TODO: WTF does filter do here?
                        Bitmap miniGeeko = Bitmap.createScaledBitmap(geeko, geeko.getWidth() / 8, geeko.getHeight() / 8, true);
                        Matrix m = new Matrix();
                        int halfW = miniGeeko.getWidth() / 2;
                        int halfH = miniGeeko.getHeight() / 2;

                        for (int i = 63; i > 0; i--) {
                            float dx = mPoints[i * 2] - mPoints[(i - 1) * 2];
                            float dy = mPoints[i * 2 + 1] - mPoints[(i - 1) * 2 + 1];
                            if (Math.abs(dx) > 20.0f || Math.abs(dy) > 20.0f) {
                                m.setRotate((float) Math.toDegrees(Math.atan2(Math.abs(dx), Math.abs(dy)) - 90.0f), halfW, halfH);
                                m.postTranslate(
                                        mPoints[(i - 1) * 2] + dx / 2 - halfW,
                                        mPoints[(i - 1) * 2 + 1] + dy / 2 - halfH);
                                c.drawBitmap(miniGeeko, m, null);
                                break;
                            }
                        }

                        /*
                        Bitmap geeko = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.geeko);
                        Matrix m = new Matrix();
                        //m.setRotate(0.0f);
                        m.setScale(0.25f, 0.25f);
                        int halfW = (int)((float)geeko.getWidth() * 0.125f);
                        int halfH = (int)((float)geeko.getHeight() * 0.125f);
                        //m.setTranslate(100.0f, 100.0f);
                        //c.drawBitmap(geeko, m, null);

                        for (int i = 63; i > 0 ; i--) {
                            float dx = mPoints[i * 2] - mPoints[(i - 1) * 2];
                            float dy = mPoints[i * 2 + 1] - mPoints[(i -1 ) * 2 + 1];
                            if (Math.abs(dx) > 10f || Math.abs(dy) > 10.0f) {
                                m.postRotate((float)Math.toDegrees(Math.atan2(dx, dy)));
                                m.postTranslate(
                                        mPoints[(i - 1) * 2] + dx / 2 - halfW,
                                        mPoints[(i -1 ) * 2 + 1] + dy / 2 - halfH);
                                c.drawBitmap(geeko, m, null);
                                break;
                            }
                        }
                        */
                        /*
                        Drawable geeko = mContext.getResources().getDrawable(R.drawable.geeko);
                        BitmapDrawable b = new BitmapDrawable(geeko);
                        int width = geeko.getIntrinsicWidth();
                        int height = geeko.getIntrinsicHeight();
                        geeko.setBounds(0, 0, width, height);
                        geeko.draw(c);*/
                    }

                    if (!mFirstBranch.equals(0,0)) {
                        mBranch = 1;
                    } else if (!mSecondBranch.equals(0,0)) {
                        mBranch = 2;
                    }
                } else if (mBranch == 1) {
                    if (!mSecondBranch.equals(0,0)) {
                        mBranch = 2;
                    } else {
                        mBranch = 0;
                        c.drawARGB(16, 70, 69, 71); // Fade
                    }
                } else {
                    mBranch = 0;
                    c.drawARGB(16, 70, 69, 71); // Fade
                }
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
                    sleep(50, 0);
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
