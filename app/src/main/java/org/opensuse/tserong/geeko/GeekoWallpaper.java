package org.opensuse.tserong.geeko;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class GeekoWallpaper extends WallpaperService {

    private final Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new GeekoEngine();
    }

    class GeekoEngine extends Engine {

        private int mCanvasWidth = -1;
        private int mCanvasHeight = -1;

        private Bitmap mBitmap;
        private Bitmap mGeeko;

        private Paint mPaint = new Paint();
        private int mPoint = 0;
        float mPoints[] = new float[128];

        float mTrunkWidth = 20.0f;
        float mFirstBranchWidth = 10.0f;
        float mSecondBranchWidth = 7.5f;
        float mGeekoScale = 1.0f;

        private Point mFirstBranch = new Point(0, 0);
        private Point mSecondBranch = new Point(0, 0);

        private int mBranch = 0;

        private int mBranchCount = 0;
        private final int mBranchesBeforeReset = 50;

        private Point mDrawGeekoAt = new Point(0, 0);
        private boolean mFlipGeeko = false;

        private long mSleepDuration = 50;

        private final Runnable mGeekoRunner = new Runnable() {
            public void run() {
                drawFrame();
            }
        };

        private boolean mVisible;

        public GeekoEngine() {
            Context context = getApplicationContext();
            mGeeko = BitmapFactory.decodeResource(context.getResources(), R.drawable.geeko_black);
            float density = context.getResources().getDisplayMetrics().density;
            mTrunkWidth *= density;
            mFirstBranchWidth *= density;
            mSecondBranchWidth *= density;
            mGeekoScale = 0.20f;
            if (density > 1.0f) {
                // This gives a geeko size that's "about right" for mdpi, xhdpi, xxhdpi,
                // but I'm sure there's some non-linear thing going on here that I'd
                // understand better if my math fu were stronger.
                mGeekoScale += 0.025 * density;
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mGeekoRunner);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (mVisible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(mGeekoRunner);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mCanvasWidth = width;
            mCanvasHeight = height;
            mBitmap = Bitmap.createBitmap(mCanvasWidth, mCanvasHeight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(mBitmap);
            c.drawARGB(255, 70, 69, 71); // Dark Grey
            mPoint = 0;
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mGeekoRunner);
        }

        /*
        @Override
        public void onOffsetsChanged(...)
        */

        private Point[] generateTrunk() {
            Point[] p = new Point[4];
            p[0] = new Point((int)(Math.random() * mCanvasWidth), mCanvasHeight + (int)(mTrunkWidth / 2));
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

        private void drawGeeko() {
            Canvas c = new Canvas(mBitmap);

            if (Math.random() > 0.25) {  // >1.0 disables geekos
                Matrix m = new Matrix();
                m.setRotate(-10.0f, 0.5f, 0.5f);
                // Translating by half the width centers geeko on the
                // x coordinate we want to draw at.  Translating by 90%
                // of the height tends to give a reasonable but not
                // entirely perfect position.
                // TODO: Ideally the rotation would match the angle of the branch,
                // and geeko's feet would both always be touching it.  It's fairly
                // goot ATM though.
                if (mFlipGeeko) {
                    m.postScale(-1, 1);
                    m.postTranslate(mGeeko.getWidth() / 2, -mGeeko.getHeight() * 0.85f);
                } else {
                    m.postTranslate(-mGeeko.getWidth() / 2, -mGeeko.getHeight() * 0.85f);
                }
                m.postScale(mGeekoScale, mGeekoScale);

                m.postTranslate(mDrawGeekoAt.x, mDrawGeekoAt.y);
                Paint p = new Paint();
                p.setColorFilter(new LightingColorFilter(0, mPaint.getColor()));
                c.drawBitmap(mGeeko, m, p);
            }

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

                        if (mBranchCount++ > mBranchesBeforeReset) {
                            mBranchCount = 0;
                            c.drawARGB(255, 70, 69, 71); // Wipe to dark grey
                        } else {
                            c.drawARGB(16, 70, 69, 71); // Fade
                        }

                        mPaint.setColor(colors[(int)(Math.random() * colors.length)]);

                        mFirstBranch.set(0, 0);
                        mSecondBranch.set(0, 0);

                        mSleepDuration = (long)(Math.random() * 25) + 25;
                }
                // Thanks http://www.fractalnet.org/CAD/curve/BezierCurve.pdf, good to see this is still online ;)
                // TODO: Could this be more sensibly implemented in terms of Android's Path class?
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
                    if (i == 32 && mBranch == 1) {
                        mFlipGeeko = bezier[3].x > bezier[0].x;
                        mDrawGeekoAt.set((int)mPoints[i * 2], (int)mPoints[i * 2 + 1]);
                    }
                }

            }

            // TODO: vines taper off too slowly on mdpi screens
            if (mBranch == 1) {
                mPaint.setStrokeWidth(Math.min(mFirstBranchWidth, 63 - mPoint));
            } else if (mBranch == 2) {
                mPaint.setStrokeWidth(Math.min(mSecondBranchWidth, 63 - mPoint));
            } else {
                mPaint.setStrokeWidth(Math.min(mTrunkWidth, 63 - mPoint));
            }
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setAntiAlias(true);

            c.drawLines(mPoints, mPoint * 2, 4, mPaint);
            mPoint++;
            if (mPoint >= 63) {
                if (mBranch == 0) {
                    if (!mFirstBranch.equals(0,0)) {
                        mBranch = 1;
                    } else if (!mSecondBranch.equals(0,0)) {
                        mBranch = 2;
                    }
                } else if (mBranch == 1) {
                    drawGeeko();
                    if (!mSecondBranch.equals(0,0)) {
                        mBranch = 2;
                    } else {
                        mBranch = 0;
                    }
                } else {
                    mBranch = 0;
                }

                if (mBranch == 0) {
                    mSleepDuration = 1000;
                }
                mPoint = 0;
            }

            canvas.drawBitmap(mBitmap, 0, 0, null);
        }

        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    doDraw(c);
                }
            } finally {
                if (c != null) {
                    holder.unlockCanvasAndPost(c);
                }
            }

            mHandler.removeCallbacks(mGeekoRunner);
            if (mVisible) {
                mHandler.postDelayed(mGeekoRunner, mSleepDuration);
            }
        }
    }

}
