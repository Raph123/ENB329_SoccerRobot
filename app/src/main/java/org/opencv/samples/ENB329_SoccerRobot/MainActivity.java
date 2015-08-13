package org.opencv.samples.ENB329_SoccerRobot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class MainActivity extends Activity implements View.OnTouchListener, CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";
    private static final String ForTesting = "YourCode::Activity!";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              TouchFlag = false;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
    private Mat mRgba;
    private Scalar box_colour = new Scalar(255,0,0,255);
    private Scalar touchColour = new Scalar(0,0,255,0);
    private Find find;
    private Mat Test_view;


    private Point p1 = new Point(0,0);
    private Point p2 = new Point(0,0);

    public Size Image_size;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.MainActivity_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);


    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    public boolean onTouch(View v, MotionEvent event){
        /*
        * On A touch event get a sample of the colour from the touched region for colour processing
        */
        int xOffset = (mOpenCvCameraView.getWidth() - mRgba.cols()) / 2; //get width of the camera view - retina columns/2
        //method to write x ofset to screen here
        int yOffset = (mOpenCvCameraView.getHeight() - mRgba.rows()) / 2;//get height of the camera view - retina columns/2
        //method to write y offset to screen here
        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;
        Log.i(ForTesting,"Touched at coordinates: " +x+", "+y);

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;
        //generate rectangle for touched region to sample the colour from
        touchedRect.width = (x+40 < mRgba.cols()) ? x + 40 - touchedRect.x : mRgba.cols() - touchedRect.x;
        touchedRect.height = (y+40 < mRgba.rows()) ? y + 40 - touchedRect.y : mRgba.rows() - touchedRect.y;

        p1.x = touchedRect.x-(touchedRect.width/2);
        p1.y = touchedRect.y+(touchedRect.height/2);
        p2.x = touchedRect.x+(touchedRect.width/2);
        p2.y = touchedRect.y-(touchedRect.height/2);
        //find.ProcFlag = true;
        touchColour = find.Channel_select_ball(mRgba.submat(touchedRect), touchedRect);

        Log.i(ForTesting, "Drew Box at: " + x + ", " + y);
        TouchFlag = true;


        return false;
    }


    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        Image_size = mRgba.size();
        find = new Find(Image_size);
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //This function feeds information to the surface/ screen
        mRgba = inputFrame.rgba();
        //TouchFlag = false;
        if(TouchFlag){
            //while(find.ProcFlag){}
            Test_view = find.Chrome(mRgba);
            Imgproc.rectangle(Test_view, p1, p2, box_colour);
            return Test_view;
        }
        return mRgba;
    }
}
