package org.opencv.samples.ENB329_SoccerRobot;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Raphael on 5/08/15.
 * This class is used to find colour and objects.
 */

public class Find {
    //Todo: Experiment using HSL to find orange as opposed to rgb

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
    private Mat zeroes;
    private double cmp_flag = 0;
    private double red_col_tol = 70; //colour tolerance
    private double green_col_tol = 50;
    private double temp;



    public Find(Size img_size){
        //Blue_ch = new Mat(img_size, CvType.CV_8UC1);
        Green_ch_upper = new Mat(img_size, CvType.CV_8UC1);
        Green_ch_lower = new Mat(img_size, CvType.CV_8UC1);
        Red_ch_upper = new Mat(img_size, CvType.CV_8UC1);
        Red_ch_lower = new Mat(img_size, CvType.CV_8UC1);
        Complete = new Mat(img_size, CvType.CV_8UC1);
        //zeroes = new Mat(img_size, CvType.CV_8UC1);
        //zeroes.zeros(img_size, CvType.CV_8UC1);

    }
    public Mat HSV_select(Mat image){
        /*
        This function is used to select an HSV value to use for thresholding as opposed to
        an RGB value.
         */
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2HSV);
        Core.extractChannel(image, Complete,0);//Extract the hue channel to the Matrix "complete"

        temp = blobColourHSV.val[0];
        //This part applies the threshold to the hue values of the image.
        if(blobColourHSV.val[0]-15>0){
            Imgproc.threshold(Complete, Red_ch_lower,(blobColourHSV.val[0]-15), 255, Imgproc.THRESH_BINARY);
        }
        else{
            Imgproc.threshold(Complete, Red_ch_lower,0, 255, Imgproc.THRESH_BINARY);
        }
        if((blobColourHSV.val[0]+15)<255){
            Imgproc.threshold(Complete, Red_ch_upper,(blobColourHSV.val[0]+15), 255, Imgproc.THRESH_BINARY);
        }
        else{
            Imgproc.threshold(Complete, Red_ch_upper,255, 255, Imgproc.THRESH_BINARY);
        }
        Core.bitwise_xor(Red_ch_upper, Red_ch_lower, Complete);

        //

        return Complete;
    }


    public Mat Threshold_select(Mat image){
        //This function thresholds the image for the selected colour (from the touched region)

        //extract red and green channels
        Core.extractChannel(image, Red_ch_upper, 0);
        Core.extractChannel(image, Green_ch_upper, 1);
        //Threshold upper and lower red channel limits
        if (chrome_colour_r-red_col_tol>0) {
            Imgproc.threshold(Red_ch_upper, Red_ch_lower, chrome_colour_r - red_col_tol+20, 255, Imgproc.THRESH_BINARY);
        }
        else{
            Imgproc.threshold(Red_ch_upper, Red_ch_lower, 0, 255, Imgproc.THRESH_BINARY);
        }
        if(chrome_colour_r+red_col_tol>255){
            Imgproc.threshold(Red_ch_upper, Red_ch_upper, 255, 255, Imgproc.THRESH_BINARY);
        }
        else{
            Imgproc.threshold(Red_ch_upper, Red_ch_upper, chrome_colour_r + red_col_tol, 255, Imgproc.THRESH_BINARY);
        }
        Core.bitwise_xor(Red_ch_upper, Red_ch_lower, Red_ch_upper);
        //Extract Green
        if (chrome_colour_g-green_col_tol>0) {
            Imgproc.threshold(Green_ch_upper, Green_ch_lower, chrome_colour_g - red_col_tol, 255, Imgproc.THRESH_BINARY);
        }
        else{
            Imgproc.threshold(Green_ch_upper, Green_ch_lower, 0, 255, Imgproc.THRESH_BINARY);
        }
        if(chrome_colour_g+green_col_tol>255){
            Imgproc.threshold(Green_ch_upper, Green_ch_upper, 255, 255, Imgproc.THRESH_BINARY);
        }
        else{
            Imgproc.threshold(Green_ch_upper, Green_ch_upper, chrome_colour_g + red_col_tol, 255, Imgproc.THRESH_BINARY);
        }

        Core.bitwise_xor(Green_ch_upper, Green_ch_lower, Green_ch_upper);

        Core.bitwise_and(Red_ch_upper, Green_ch_upper, Complete);
        //Todo: Apply smoothing operations. Erode, dialate, blur

        return Complete;
    }

    public Scalar Channel_select_ball(Mat colour_sampleRGB, Rect touched_region){
        /*
        This function will take the colour of the selected region  and transform it to a scalar
        value to use for image processing and thresholding
         */

        Mat colour_sampleHSV = new Mat();
        Imgproc.cvtColor(colour_sampleRGB, colour_sampleHSV, Imgproc.COLOR_RGB2HSV);
        blobColourHSV = Core.sumElems(colour_sampleHSV);
        int pixels_in_region = touched_region.width * touched_region.height;

        for (int i = 0; i<blobColourHSV.val.length; i++){
            temp = blobColourHSV.val[i];
            blobColourHSV.val[i]/=pixels_in_region;
        }

        blobColorRGB = convertHSV2RGB(blobColourHSV);

        //chrome_colour_y = blobColorRGB.val[0]+blobColorRGB.val[1]+blobColorRGB.val[2];
        chrome_colour_r = blobColorRGB.val[0];///chrome_colour_y;
        chrome_colour_g = blobColorRGB.val[1];///chrome_colour_y;
        //For some reason Mat type objects don't like decimals
        //ToDo: Figure out why MAT types won't accept decimals. Might be to do with data type CVU8C1

        return blobColorRGB;
    }




    private Scalar convertHSV2RGB(Scalar hsvColor){
        //Mat pointMatRgba = new Mat();
        Mat pointMatConvert = new Mat(1,1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatConvert,pointMatConvert,Imgproc.COLOR_HSV2RGB_FULL);
        return new Scalar(pointMatConvert.get(0,0));
    }

}
