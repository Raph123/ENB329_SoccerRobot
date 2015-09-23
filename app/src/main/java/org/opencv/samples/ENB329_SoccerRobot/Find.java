package org.opencv.samples.ENB329_SoccerRobot;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Raphael on 5/08/15.
 * This class is used to find colour and objects.
 * see java openCV API at
 * http://docs.opencv.org/java/
 */

public class Find {
    //public variables
    public Mat Complete;
    public Point Center;
    public int radius;

    //objects
    Mat mHierarchy = new Mat();


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
    private Size imgResize;
    private Size imgSize;
    private double sizeFactor;
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();


    public Find(Size img_size){
        sizeFactor = 4;
        imgResize = new Size ((img_size.width/sizeFactor), (img_size.height/sizeFactor));
        imgSize = new Size(img_size.width, img_size.height);

        Red_ch_upper = new Mat(imgResize, CvType.CV_8UC1);
        Red_ch_lower = new Mat(imgResize, CvType.CV_8UC1);
        imageHSV = new Mat(imgResize, CvType.CV_8UC1);
        Complete = new Mat(img_size, CvType.CV_8UC1);
        imageResized = new Mat (imgResize, CvType.CV_8UC1);

        Kernel = new Mat(5,5,CvType.CV_8U);

        Hue_ch = new Mat(img_size, CvType.CV_8U);
        Saturation_ch = new Mat(img_size, CvType.CV_8U);
        Light_ch = new Mat(img_size, CvType.CV_8U);



        Center = new Point();
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
        if(hue-10>0){
            Imgproc.threshold(Hue_ch, Red_ch_lower,(hue-10), 255, Imgproc.THRESH_BINARY);
        }
        else{
            Imgproc.threshold(Hue_ch, Red_ch_lower,0, 255, Imgproc.THRESH_BINARY);
        }
        if((hue+10)<255){
            Imgproc.threshold(Hue_ch, Red_ch_upper,(hue+10), 255, Imgproc.THRESH_BINARY);
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
      }

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
        Imgproc.cvtColor(imageResized, imageHSV, Imgproc.COLOR_RGB2HSV);
        Core.extractChannel(imageHSV, Light_ch, 2);
        Core.extractChannel(imageHSV, Saturation_ch, 1);

        //Imgproc.threshold(Saturation_ch, Red_ch_lower, saturation, 255, Imgproc.THRESH_BINARY);
        Imgproc.threshold(Light_ch, imageResized, light, 255, Imgproc.THRESH_BINARY);

        //Core.bitwise_not(Red_ch_lower, Saturation_ch);
        Core.bitwise_not(imageResized, Light_ch);
        //Core.bitwise_and(Saturation_ch, Light_ch, Red_ch_lower);

        Imgproc.GaussianBlur(Light_ch, imageResized, Kernel.size(), 2, 2);

        //Imgproc.resize(imageResized, Complete, imgSize);
        Imgproc.resize(Light_ch, Complete, imgSize);


        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(Light_ch, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//
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
//
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

}
