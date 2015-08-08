package org.opencv.samples.ENB329_SoccerRobot;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by Raphael on 5/08/15.
 * This class is used to find colour and objects.
 */

public class Find {

    private Scalar blobColourHSV;
    private Scalar blobColorRGB;
    private double chrome_colour_r;
    private double chrome_colour_g;
    private double chrome_colour_y;
    //private Mat Blue_ch;
    private Mat Green_ch_upper;
    private Mat Green_ch_lower;
    private Mat Red_ch_upper;
    private Mat Red_ch_lower;
    private Mat Complete;
    //private Mat zeroes;
    private double img_rows, img_cols;
    private List Mat_chs;
    private double cmp_flag = 0;
    private double red_col_tol = 50; //colour tolerance
    private double green_col_tol = 50;



    public Find(Size img_size){
        //Blue_ch = new Mat(img_size, CvType.CV_8UC1);
        Green_ch_upper = new Mat(img_size, CvType.CV_8UC1);
        Green_ch_lower = new Mat(img_size, CvType.CV_8UC1);
        Red_ch_upper = new Mat(img_size, CvType.CV_8UC1);
        Red_ch_lower = new Mat(img_size, CvType.CV_8UC1);
        Complete = new Mat(img_size, CvType.CV_8UC1);
        //zeroes = new Mat(img_size, CvType.CV_8UC1);
        //zeroes.zeros(img_size, CvType.CV_8UC1);
//        img_cols = img_size.width;
//        img_rows = img_size.height;
    }
    public Mat Threshold_select(Mat image){
        //This function thresholds the image for the selected

        //extract red and green channels
        Core.extractChannel(image, Red_ch_upper, 0);
        Core.extractChannel(image, Green_ch_upper, 1);
        //Threshold upper and lower red channel limits
        Imgproc.threshold(Red_ch_upper, Red_ch_lower, chrome_colour_r - red_col_tol, 255, Imgproc.THRESH_BINARY);
        Imgproc.threshold(Red_ch_upper, Red_ch_upper, chrome_colour_r + red_col_tol, 255, Imgproc.THRESH_BINARY);
        Core.bitwise_xor(Red_ch_upper, Red_ch_lower, Red_ch_upper);
        //threshold upper and lower green channel limits
        Imgproc.threshold(Green_ch_upper, Green_ch_lower, chrome_colour_g + green_col_tol, 255, Imgproc.THRESH_BINARY);
        Imgproc.threshold(Green_ch_upper, Green_ch_upper, chrome_colour_g - green_col_tol, 255, Imgproc.THRESH_BINARY);
        Core.bitwise_xor(Green_ch_upper, Green_ch_lower, Green_ch_upper);
        //Combine thresholds
        Core.bitwise_and(Red_ch_upper, Green_ch_upper, Complete);

        //Core.compare(Red_ch, Green_ch,Complete, cmp_flag);
        return Complete;
    }

    public Scalar Channel_select_ball(Mat colour_sampleRGB, Rect touched_region){

        Mat colour_sampleHSV = new Mat();
        Imgproc.cvtColor(colour_sampleRGB, colour_sampleHSV, Imgproc.COLOR_RGB2HSV_FULL);
        blobColourHSV = Core.sumElems(colour_sampleHSV);
        int pixels_in_region = touched_region.width * touched_region.height;

        for (int i = 0; i<blobColourHSV.val.length; i++){
            blobColourHSV.val[i]/=pixels_in_region;
        }

        blobColorRGB = convertHSV2RGB(blobColourHSV);

        //chrome_colour_y = blobColorRGB.val[0]+blobColorRGB.val[1]+blobColorRGB.val[2];
        chrome_colour_r = blobColorRGB.val[0];///chrome_colour_y;
        chrome_colour_g = blobColorRGB.val[1];///chrome_colour_y;

        return blobColorRGB;
    }




    private Scalar convertHSV2RGB(Scalar hsvColor){
        //Mat pointMatRgba = new Mat();
        Mat pointMatConvert = new Mat(1,1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatConvert,pointMatConvert,Imgproc.COLOR_HSV2RGB_FULL);
        return new Scalar(pointMatConvert.get(0,0));
    }

}
