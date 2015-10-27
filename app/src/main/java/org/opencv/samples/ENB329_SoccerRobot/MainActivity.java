package org.opencv.samples.ENB329_SoccerRobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.opencv.imgproc.Imgproc.rectangle;


public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;

    private Mat mRgba;
    public Size Image_size;

    private Find ball;
    private Find obstacle;

    //widgets--------------------------
    private Button findObstacle;
    private Button findBall;
    private Button goalFind;
    private SeekBar Hue1;
    private SeekBar saturation_min;
    private SeekBar light_min;

//-----------Image processing paramter variables--------------
    private int ballHue;
    private int ballSaturation;
    private int ballLight;
    private int obstacleHue;
    private int obstacleSaturation;
    private int obstacleLight;
    private int goalHue;
    private int goalSaturation;
    private int goalLight;

    public boolean ballView;
    public boolean obstacleView;
    public boolean goalView;
    public boolean goalSet;

    public Point obstacle_location;

    private BT_helper BTCom;

    private Move motorControl;

//-------Bluetooth Connection -----------------------------------//
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView lv;
    private BluetoothDevice mDevice;
    char[] testArray = new char[] {72,69,76};

    private org.opencv.core.Rect rect;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
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
        rect = new Rect();
        ballView = true;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.MainActivity_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
//----------Initialize Sliders----------------------------------------------------------------------
        SeekBar Hue1 = (SeekBar) findViewById(R.id.Hue1);//initialize seek bar
        SeekBar saturation_min = (SeekBar) findViewById(R.id.saturation_min);
        SeekBar light_max = (SeekBar) findViewById(R.id.Light_max);

//----------Bluetooth stuff--------------
        //ToDo: Turn this back on when testing with robot
        motorControl = new Move();
        BTCom = new BT_helper();
        try{
            BTCom.openCommunication();
        } catch(IOException ex){
            Log.i(TAG, "bluetooth failed to open, this will probs crash");
        }


//----------Slider functions------------------------------------------------------------------------

        Hue1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar Hue1, int progress, boolean fromUser) {
                if (ballView){
                    ballHue = progress;
                }
                else if (obstacleView){
                    obstacleHue = progress;
                }
                else if (goalView){

                }
                else if (goalSet){
                    goalHue = progress;
                }

            }
            @Override
            public void onStartTrackingTouch(SeekBar Hue1) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar Hue1) {
            }
        });

        saturation_min.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar saturation_min, int progress, boolean fromUser) {
                if (ballView){
                    ballSaturation = progress;
                }
                else if(obstacleView){
                    obstacleSaturation = progress;
                }
                else if(goalView){
                    obstacleSaturation = progress;
                }
                else if (goalSet){
                    goalSaturation = progress;
                }

            }
            @Override
            public void onStartTrackingTouch(SeekBar saturation_min) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar saturation_min) {
            }
        });
        light_max.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar light_max, int progress, boolean fromUser) {
                if (ballView){
                    ballLight = progress;
                }
                else if (obstacleView){
                    obstacleLight = progress;
                }
                else if (goalSet){
                    goalLight = progress;
                }

            }
            @Override
            public void onStartTrackingTouch(SeekBar light_max) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar light_max) {
            }

        });
//-----------Toggle Button----------------------------------------------------------------------------
        findBall = (Button) findViewById(R.id.Ball);
        findObstacle = (Button) findViewById(R.id.obstacle);
        goalFind = (Button) findViewById(R.id.goalSet);

        //mConnectThread = new ConnectThread(mDevice);
        //mConnectThread.start();

    }

    public void setBallColour(View v){
        ballView = !ballView;
        obstacleView = false;
        goalView = false;
        goalSet = false;
    }
    public void setObstacleColour(View v){
        obstacleView = !obstacleView;
        ballView = false;
        goalView = false;
        goalSet = false;
    }

    public void setGoalColour(View v)
    {
        goalView = !goalView;
        obstacleView = false;
        ballView = false;
        goalSet = false;
    }
    public void setGoal(View v){
        goalSet = !goalSet;
        goalView = false;
        obstacleView = false;
        ballView = false;
    }
    @Override
    public void onPause() {
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



    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        Image_size = mRgba.size();
        ball = new Find(Image_size);
        obstacle = new Find(Image_size);
        ball.setResizeFactor(3);
        obstacle.setResizeFactor(6);
        obstacle_location = new Point(0, 0);
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //when a camera frame is available
        mRgba = inputFrame.rgba();
        //Process the images
//---------------------These are the finding functions ----------------

        //if found --> find goal
        //if not found -- do ball.findcircle
//        if (ball.dribbling) {
//            //search for goal
//            ball.findGoal(mRgba, goalHue, goalSaturation, goalLight);
//        }
//        else{
        ball.findCircle(mRgba, ballHue, ballSaturation, ballLight);
//        }

        obstacle.findObstacle(mRgba, obstacleLight, obstacleSaturation);
        //add search for the wall
        //relate the gradient of the wall to the distance of the robot to the wall
//---------------------------------------------------------------------
        if(ballView){
            Imgproc.circle(ball.Complete, ball.Center, ball.radius, new Scalar(255,0,0,255), 4);
            testArray[0] = 'Y';
            testArray[1] = 'Y';
            testArray[2] = 'Y';

            try{
                BTCom.sendData(testArray);
            }catch(IOException ex){
                Log.i(TAG, "bluetooth failed to send data");
                Log.e(TAG, "bluetooth failed to send data");
            }

            return ball.Complete;
        }
//        if(ballView){
//            Imgproc.circle(ball.Complete, ball.Center, ball.radius, new Scalar(255,0,0,255), 4);
//            testArray[0] = 'Y';
//            testArray[1] = 'Y';
//            testArray[2] = 'Y';
//
//            try{
//                BTCom.sendData(testArray);
//            }catch(IOException ex){
//                Log.i(TAG, "bluetooth failed to send data");
//                Log.e(TAG, "bluetooth failed to send data");
//            }
//
//            return ball.Complete;
//        }
        else if(obstacleView){
            //Todo: create an obstacle method in the find class, or create a find class for obstacles

            List <MatOfPoint> contours = obstacle.getContours();
            testArray[0] = 'Y';
            testArray[1] = 'Y';
            testArray[2] = 'Y';

            try{
                BTCom.sendData(testArray);
            }catch(IOException ex){
                Log.i(TAG, "bluetooth failed to send data");
                Log.e(TAG, "bluetooth failed to send data");
            }
            //Imgproc.drawContours(obstacle.Complete, wall, -1, new Scalar (255,0,0,255));

            return obstacle.Complete;
        }
        else if (goalView){
            testArray[0] = 'Y';
            testArray[1] = 'Y';
            testArray[2] = 'Y';

            try{
                BTCom.sendData(testArray);
            }catch(IOException ex){
                Log.i(TAG, "bluetooth failed to send data");
                Log.e(TAG, "bluetooth failed to send data");
            }
            return obstacle.Complete1;
        }
        else if(goalSet){
            ball.findGoal(mRgba, goalHue, goalSaturation, goalLight);


            testArray[0] = 'Y';
            testArray[1] = 'Y';
            testArray[2] = 'Y';
            try{
                BTCom.sendData(testArray);
            }catch(IOException ex){
                Log.i(TAG, "bluetooth failed to send data");
                Log.e(TAG, "bluetooth failed to send data");
            }
            return ball.Complete;
        }
        else{
            List<MatOfPoint> contours = obstacle.getContours();
            //List<MatOfPoint> wall = obstacle.getWallContours();
            Imgproc.circle(mRgba, ball.Center, ball.radius, new Scalar(0, 255, 0, 255), 4);
            Log.i(TAG, "Ball at at x =" + ball.Center.x + " ,y =" + ball.Center.y);
            //Check whether to look for goal or not
 //           if ((ball.Center.x > 100)||(ball.radius == 0)){
                //testArray = motorControl.find_ball(ball.Center,rect );
//            }
//            else{
//
//            }

            //Draw objects to surface
            Imgproc.line(mRgba, obstacle.getWallP1(), obstacle.getWallP2(), new Scalar(255,0,0,255), 2);
            for(int i = 0; i<contours.size();i++){
                obstacle_location.x = 0;
                obstacle_location.y = 0;
                if (Imgproc.contourArea(contours.get(i)) > 20 ){
                    rect = Imgproc.boundingRect(contours.get(i));
                    if(Imgproc.contourArea(contours.get(i))/ (rect.width*rect.height) > 0.75) {
                        obstacle_location.x = rect.x+(rect.width/2);
                        obstacle_location.y = rect.y+(rect.height/2);
                        rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255, 255));
                        Log.i(TAG, "Obstacle at x =" + obstacle_location.x + " ,y =" + obstacle_location.y);
                        //rectangle (mRgba, new Point(0,0), new Point(250, 250), new Scalar (255,0,0,255));
                    }
                    else{
                        Log.i(TAG, "No Obstacles found");
                    }
                }
            }

//            if(ball.dribbling){
                testArray = motorControl.find_ball(ball.radius, ball.Center, rect, obstacle.getWallP1(), obstacle.getWallP2());
//            }
//            else{
//                testArray = motorControl.find_ball(ball.rect.width, ball.Center, rect, obstacle.getWallP1(), obstacle.getWallP2(), ball.dribbling);
//            }
            if(ball.dribbling){
                testArray[0] = 108;
                testArray[1] = 'Y';
                testArray[2] = 99;
            }

            //ToDo: Turn this back on when testing with robot
            try{
                BTCom.sendData(testArray);
            }catch(IOException ex){
                Log.i(TAG, "bluetooth failed to send data");
                Log.e(TAG, "bluetooth failed to send data");
            }
            return mRgba;
        }

    }


}

