package org.opencv.samples.ENB329_SoccerRobot;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.opencv.imgproc.Imgproc.rectangle;


/**
 * Created by Raphael on 5/08/15.
 * This class is used to find colour and objects.
 * see java openCV API at
 * http://docs.opencv.org/java/
 */

public class Find {
    //public variables
    public Mat Complete;
    public Mat Complete1;
    public Point Center;
    public int radius;
    public boolean dribbling;
    public Point goal_location;
    public org.opencv.core.Rect rect;

    //objects
    Mat mHierarchy = new Mat();
    private Point wallP1 = new Point();
    private Point wallP2 = new Point();

    private Mat Red_ch_upper;
    private Mat Red_ch_lower;

    private Mat Kernel;

    private Mat Circles_out = new Mat();

    //HSV Channels
    private Mat Hue_ch;
    private Mat Saturation_ch;
    private Mat Light_ch;
    private Mat imageHSV;
    private Mat imageResized;
    private  Mat imageResized2;
    private Mat wallEdges;
    private Size imgResize;
    private Size imgSize;
    private double sizeFactor;
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
    private List<MatOfPoint> mWalls = new ArrayList<MatOfPoint>();

    private Mat lines = new Mat();

    public Find(Size img_size){
        wallEdges = new Mat(img_size,  CvType.CV_8UC1);

        sizeFactor = 4;
        imgResize = new Size ((img_size.width/sizeFactor), (img_size.height/sizeFactor));
        imgSize = new Size(img_size.width, img_size.height);

        Red_ch_upper = new Mat(imgResize, CvType.CV_8UC1);
        Red_ch_lower = new Mat(imgResize, CvType.CV_8UC1);
        imageHSV = new Mat(imgResize, CvType.CV_8UC1);
        Complete = new Mat(img_size, CvType.CV_8UC1);
        Complete1 = new Mat(img_size, CvType.CV_8UC1);
        imageResized = new Mat (imgResize, CvType.CV_8UC1);
        imageResized2 = new Mat (imgResize, CvType.CV_8UC1);
        Kernel = new Mat(5,5,CvType.CV_8U);

        Hue_ch = new Mat(img_size, CvType.CV_8U);
        Saturation_ch = new Mat(img_size, CvType.CV_8U);
        Light_ch = new Mat(img_size, CvType.CV_8U);

        Center = new Point();
        rect = new Rect();
        dribbling = false;
        goal_location = new Point();
    }


    public void findCircle(Mat image, int hue, int saturation, int light){
        /*
        This function is used to select an HSV value to use for thresholding. It will output the
        image with the largest circle of desired hue encricled.
        Input parameters
        - image: the image that is going to be processed. Must be of type MAT
        - hue: The desired hue. Every pixel that has a value of +-10units from the desired hue will
                be processed.
        - saturation: The desired minimum saturation. This is the minimum saturation value
        that a pixel can have in order to be processed. All pixels with a lower value will be ignored
        - light: The minimum desired light value. All pixels with a light value that is lower than
        this will be ignored.

         */
        Imgproc.resize(image, imageResized, imgResize);
        Imgproc.cvtColor(imageResized, imageHSV, Imgproc.COLOR_RGB2HSV);
        Core.extractChannel(imageHSV, Hue_ch, 0);//Extract the hue channel to the Matrix "complete"
        Core.extractChannel(imageHSV, Saturation_ch, 1);
        Core.extractChannel(imageHSV, Light_ch, 2);

        //This part applies the threshold to the hue values of the image.
        if(hue-7>0){
            Imgproc.threshold(Hue_ch, Red_ch_lower,(hue-7), 255, Imgproc.THRESH_BINARY);
        }
        else{
            Imgproc.threshold(Hue_ch, Red_ch_lower,0, 255, Imgproc.THRESH_BINARY);
        }
        if((hue+7)<255){
            Imgproc.threshold(Hue_ch, Red_ch_upper,(hue+7), 255, Imgproc.THRESH_BINARY);
        }
        else{
            Imgproc.threshold(Hue_ch, Red_ch_upper,255, 255, Imgproc.THRESH_BINARY);
        }
        Core.bitwise_xor(Red_ch_upper, Red_ch_lower, Hue_ch);

        Imgproc.threshold(Saturation_ch, Saturation_ch, saturation, 255, Imgproc.THRESH_BINARY);
        Imgproc.threshold(Light_ch, Light_ch, light, 255, Imgproc.THRESH_BINARY);


        Core.bitwise_and(Hue_ch, Saturation_ch, imageResized);
        Core.bitwise_and(imageResized, Light_ch, imageResized);

        Imgproc.GaussianBlur(imageResized, imageResized, Kernel.size(), 2, 2);


        Imgproc.HoughCircles(imageResized, Circles_out, Imgproc.CV_HOUGH_GRADIENT, 2, 150, 200, 18, 1, 150);
        Imgproc.resize(imageResized, Complete, imgSize);

        //Todo: find an algorithm to relate coordinates in the decreased image, with coordis in original image
        float circle[] = new float[3];

        radius = 0;
        for (int j=0; j<Circles_out.cols();j++ ){
            Circles_out.get(0,j,circle);
            if((int)(circle[2])>=(radius)){
                radius = (int)sizeFactor*(int)circle[2]; //scale factor of 2 because of the resizing operations.
                Center.x = sizeFactor*circle[0];
                Center.y = sizeFactor*circle[1];
            }
        }
        Complete.submat(0, Complete.height(), 0, 30);
        dribbling = checkBall(Complete.submat(0, Complete.height(), 0, 20));
      }
//    public boolean dribble(Mat image, int light, int saturation, int hue){
//        //If the ball is in the dribbling zone, return true, otherwise return false
//
//        return false;
//    }

    public void findObstacle(Mat image, int light, int saturation){
        /*
        This function is used to select an HSV value to use for thresholding. It will output the
        image with the largest circle of desired hue encricled.
        Input parameters
        - image: the image that is going to be processed. Must be of type MAT
        - hue: The desired hue. Every pixel that has a value of +-10units from the desired hue will
                be processed.
        - saturation: The desired minimum saturation. This is the minimum saturation value
        that a pixel can have in order to be processed. All pixels with a lower value will be ignored
        - light: The minimum desired light value. All pixels with a light value that is lower than
        this will be ignored.
        This code is made with the assumption that the obstacles are black
         */
        Imgproc.resize(image, imageResized, imgResize);
        //Imgproc.resize(image, imageResized2, imgResize);
        Imgproc.cvtColor(imageResized, imageHSV, Imgproc.COLOR_RGB2HSV);
        //Imgproc.cvtColor(imageResized2, imageHSV, Imgproc.COLOR_RGB2HSV);
        Core.extractChannel(imageHSV, Light_ch, 2);
        Core.extractChannel(imageHSV, Saturation_ch, 1);

        Imgproc.threshold(Saturation_ch, imageResized2, saturation, 255, Imgproc.THRESH_BINARY);
        //Imgproc.threshold(Saturation_ch, Saturation_ch, saturation, 255, Imgproc.THRESH_BINARY);
        Imgproc.threshold(Light_ch, imageResized, light, 255, Imgproc.THRESH_BINARY);

        Core.bitwise_not(imageResized2, Saturation_ch);
        Core.bitwise_not(imageResized, Light_ch);
        //Core.bitwise_and(Saturation_ch, Light_ch, Red_ch_lower);

        Imgproc.GaussianBlur(Light_ch, imageResized, Kernel.size(), 2, 2);
        Imgproc.resize(Light_ch, Complete, imgSize);
        Imgproc.resize(Saturation_ch, Complete1, imgSize);
        //Imgproc.resize(imageResized, Complete, imgSize);
        //Imgproc.resize(Light_ch, Complete, imgSize);
        //Imgproc.resize(Saturation_ch, Complete, imgSize);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(Light_ch, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

//        //find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()){
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea){
                maxArea = area;
            }
        }
//        //filter contours by area and resize to fit original image
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext())  {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > 0.6*maxArea) { //0.1 is the minimum contour area
                Core.multiply(contour, new Scalar(sizeFactor,sizeFactor), contour);
                mContours.add(contour);
            }
        }

//this is for the wall countours
        Imgproc.Canny(Saturation_ch, Saturation_ch, 50, 200);

        Imgproc.HoughLinesP(Saturation_ch, lines, 1, Math.PI / 180, 40, 20, 20);
        for (int x = 0; x < lines.cols(); x++)
        {
            double[] vec = lines.get(0, x);
            double  x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            wallP1.x = x1*sizeFactor;
            wallP1.y = y1* sizeFactor;
            wallP2.x = x2* sizeFactor;
            wallP2.y = y2* sizeFactor;
            //Imgproc.line(Saturation_ch, start, end, new Scalar(255, 0, 0), 3);
            Log.i("Line Point", "start: " + x1 + ", " + y1);
        }


    }

    public Point getWallP1(){
        return wallP1;
    }
    public  Point getWallP2(){
        return wallP2;
    }

    public List<MatOfPoint> getContours() {
        return mContours;
    }


    public void setResizeFactor(double x){
        sizeFactor = x;
        imgResize = new Size ((imgSize.width/sizeFactor), (imgSize.height/sizeFactor));
        imgSize = new Size(imgSize.width, imgSize.height);

        Red_ch_upper = new Mat(imgResize, CvType.CV_8UC1);
        Red_ch_lower = new Mat(imgResize, CvType.CV_8UC1);
        imageHSV = new Mat(imgResize, CvType.CV_8UC1);
        Complete = new Mat(imgSize, CvType.CV_8UC1);
        imageResized = new Mat (imgResize, CvType.CV_8UC1);
    }

    public boolean checkBall(Mat dribbleSquare){
        //submat should be x = 0 -> 30px and  binary

        int n = Core.countNonZero(dribbleSquare);
        int m = dribbleSquare.height()*30;
        if((n/m)>0.5){
            Log.i("Found the ball!", "ball is being dribbled");
            return true;
        }
        return false;
    }

//    public void findGoal(Mat image){
//
//    }
    public void findGoal(Mat image, int hue, int light, int saturation){
            /*
            This function is used to select an HSV value to use for thresholding. It will output the
            image with the largest circle of desired hue encricled.
            Input parameters
            - image: the image that is going to be processed. Must be of type MAT
            - hue: The desired hue. Every pixel that has a value of +-10units from the desired hue will
                    be processed.
            - saturation: The desired minimum saturation. This is the minimum saturation value
            that a pixel can have in order to be processed. All pixels with a lower value will be ignored
            - light: The minimum desired light value. All pixels with a light value that is lower than
            this will be ignored.
            This code is made with the assumption that the obstacles are black
             */
        Imgproc.resize(image, imageResized, imgResize);
        Imgproc.cvtColor(imageResized, imageHSV, Imgproc.COLOR_RGB2HSV);
        Core.extractChannel(imageHSV, Hue_ch, 0);//Extract the hue channel to the Matrix "complete"
        Core.extractChannel(imageHSV, Saturation_ch, 1);
        Core.extractChannel(imageHSV, Light_ch, 2);

        //This part applies the threshold to the hue values of the image.
        if(hue-7>0){
            Imgproc.threshold(Hue_ch, Red_ch_lower,(hue-7), 255, Imgproc.THRESH_BINARY);
        }
        else{
            Imgproc.threshold(Hue_ch, Red_ch_lower,0, 255, Imgproc.THRESH_BINARY);
        }
        if((hue+7)<255){
            Imgproc.threshold(Hue_ch, Red_ch_upper,(hue+7), 255, Imgproc.THRESH_BINARY);
        }
        else{
            Imgproc.threshold(Hue_ch, Red_ch_upper,255, 255, Imgproc.THRESH_BINARY);
        }
        Core.bitwise_xor(Red_ch_upper, Red_ch_lower, Hue_ch);

        Imgproc.threshold(Saturation_ch, Saturation_ch, saturation, 255, Imgproc.THRESH_BINARY);
        Imgproc.threshold(Light_ch, Light_ch, light, 255, Imgproc.THRESH_BINARY);


        Core.bitwise_and(Hue_ch, Saturation_ch, imageResized);
        Core.bitwise_and(imageResized, Light_ch, imageResized);

        Imgproc.GaussianBlur(imageResized, imageResized, Kernel.size(), 2, 2);
        Imgproc.resize(imageResized, Complete, imgSize);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(Light_ch, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

    //        //find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()){
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea){
                maxArea = area;
            }
        }
    //        //filter contours by area and resize to fit original image
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext())  {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > 0.6*maxArea) { //0.1 is the minimum contour area
                Core.multiply(contour, new Scalar(sizeFactor,sizeFactor), contour);
                mContours.add(contour);
            }
        }

        for(int i = 0; i<contours.size();i++){
            goal_location.x = 0;
            goal_location.y = 0;
            if (Imgproc.contourArea(contours.get(i)) > 20 ){
                rect = Imgproc.boundingRect(contours.get(i));
                if(Imgproc.contourArea(contours.get(i))/ (rect.width*rect.height) > 0.75) {
                    goal_location.x = rect.x+(rect.width/2);
                    goal_location.y = rect.y+(rect.height/2);
                    Center.x = goal_location.x;
                    Center.y = goal_location.y;
                    rectangle(Complete, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255, 255));
                    Log.i("Goal", "Obstacle at x =" + goal_location.x + " ,y =" + goal_location.y);
                    //rectangle (mRgba, new Point(0,0), new Point(250, 250), new Scalar (255,0,0,255));
                }
                else{
                    Log.i("Goal", "No Obstacles found");
                }
            }
        }




    }




}
