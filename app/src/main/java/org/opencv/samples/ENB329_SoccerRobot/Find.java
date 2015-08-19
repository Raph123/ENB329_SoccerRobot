package org.opencv.samples.ENB329_SoccerRobot;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


/**
 * Created by Raphael on 5/08/15.
 * This class is used to find colour and objects.
 */

public class Find {

    private Mat Red_ch_upper;
    private Mat Red_ch_lower;
    public Mat Complete;
    private Mat Kernel;

    //moments
    private Mat Circles_out = new Mat();

    //HSV Channels
    private Mat Hue_ch;
    private Mat Saturation_ch;
    private Mat Light_ch;
    private Mat imageHSV;


    public Find(Size img_size){
        Red_ch_upper = new Mat(img_size, CvType.CV_8UC1);
        Red_ch_lower = new Mat(img_size, CvType.CV_8UC1);
        imageHSV = new Mat(img_size, CvType.CV_8UC1);
        Complete = new Mat(img_size, CvType.CV_8UC1);

        Kernel = new Mat(9,9,CvType.CV_8U);

        Hue_ch = new Mat(img_size, CvType.CV_8U);
        Saturation_ch = new Mat(img_size, CvType.CV_8U);
        Light_ch = new Mat(img_size, CvType.CV_8U);

    }


    public Mat HSV_select(Mat image, int hue, int saturation, int light){
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
        Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_RGB2HSV);
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


        Core.bitwise_and(Hue_ch, Saturation_ch, Complete);
        Core.bitwise_and(Complete, Light_ch,Complete);

        Imgproc.GaussianBlur(Complete, Complete, Kernel.size(), 2, 2);
        Imgproc.HoughCircles(Complete,Circles_out,Imgproc.CV_HOUGH_GRADIENT,2, Complete.rows()/7, 200, 50, 10, 150);

        float circle[] = new float[3];
        Point Center = new Point();

        int radius = 0;
        for (int j=0; j<Circles_out.cols();j++ ){
            Circles_out.get(0,j,circle);
            if((int)circle[2]>=radius){
                radius = (int)circle[2];
                Center.x = circle[0];
                Center.y = circle[1];
            }
        }
        //Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_RGB2HSV);
        Imgproc.circle(image, Center, radius, new Scalar(255, 0, 0, 255), 1);
        Imgproc.circle(Complete, Center, radius, new Scalar(255, 0, 0, 255), 1);
        return image;
    }
}
