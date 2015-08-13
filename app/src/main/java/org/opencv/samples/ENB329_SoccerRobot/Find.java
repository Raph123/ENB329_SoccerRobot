package org.opencv.samples.ENB329_SoccerRobot;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
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

    private Mat Blue_ch;
    private Mat Green_ch_upper;
    private Mat Green_ch_lower;
    private Mat Red_ch_upper;
    private Mat Red_ch_lower;
    private Mat Temp_mat;
    private Mat Complete;
    private Mat Sum_of_channels;

    public boolean ProcFlag;
    private Mat zeroes;
//image smoothing matricies
    private Mat Kernel;
    private  Mat Kernel_L;
    private Mat Kernel_s;

    private double red_col_tol_u = 0.1; //colour tolerance
    private double red_col_tol_l = 0.1;
    private double green_col_tol_u = 0.1;
    private double green_col_tol_l = 0.2;

    private double red_upper_threshold;
    private double red_lower_threshold;
    private double green_upper_threshold;
    private  double green_lower_threshold;

    //moments
    private Mat Circles_out = new Mat();


    public Find(Size img_size){
        Blue_ch = new Mat(img_size, CvType.CV_8UC1);
        Green_ch_upper = new Mat(img_size, CvType.CV_8UC1);
        Green_ch_lower = new Mat(img_size, CvType.CV_8UC1);
        Red_ch_upper = new Mat(img_size, CvType.CV_8UC1);
        Red_ch_lower = new Mat(img_size, CvType.CV_8UC1);

        Temp_mat = new Mat(img_size, CvType.CV_32F);
        Complete = new Mat(img_size, CvType.CV_8UC1);
        Sum_of_channels = new Mat(img_size, CvType.CV_16UC1);
        ProcFlag = true;
        Kernel = new Mat(25,25,CvType.CV_8U);
        Kernel_L = new Mat(50,50,CvType.CV_8U);
        Kernel_s = new Mat(15, 15, CvType.CV_8U);

        //zeroes = new Mat(img_size, CvType.CV_8UC1);
        //zeroes.zeros(img_size, CvType.CV_8UC1);

        //moments


    }

    public Mat Chrome(Mat image){
        //this function will chromatise the image
        //green_upper_threshold = chrome_colour_g+green_col_tol_u;
        //while(ProcFlag == true){}
        //Extract all channels

        //double i, j, k;
        Point Center = new Point();
        int radius = 0;
        Core.extractChannel(image, Red_ch_upper, 0);//extract red channel to Mat red_ch)upper
        Core.extractChannel(image, Green_ch_upper, 1);// Extract green channel to Mat Green_ch_upper
        Core.extractChannel(image, Blue_ch, 2);// extract blue channel to Mat Blue_ch

        green_upper_threshold = ((chrome_colour_g+green_col_tol_u)<1) ? chrome_colour_g+green_col_tol_u : 1;
        green_lower_threshold = ((chrome_colour_g-green_col_tol_l)>0) ? chrome_colour_g - green_col_tol_l : 0;

        red_upper_threshold = ((chrome_colour_r+red_col_tol_u)<1) ? chrome_colour_r+red_col_tol_u : 1;
        red_lower_threshold = ((chrome_colour_r - red_col_tol_l) > 0) ? chrome_colour_r - red_col_tol_l: 0;

        //Add channel values
        Core.add(Red_ch_upper, Green_ch_upper, Sum_of_channels);//Add the red channel and the green channel and store result in green_ch_lower
        Core.add(Blue_ch, Sum_of_channels, Sum_of_channels);//Add the blue channel and store to green_ch_lower

        //Chromatise and make binary the red channel
        Core.divide(Red_ch_upper, Sum_of_channels, Temp_mat);//divide red channel by the sum of channels to obtain chrome temp store red chrome in complete
        Imgproc.threshold(Temp_mat, Red_ch_lower, chrome_colour_r-0.1, 255, Imgproc.THRESH_BINARY);//threshold everything above desired chrome value and save to Red_ch_lower
        Imgproc.threshold(Temp_mat, Red_ch_upper, chrome_colour_r + 0.2, 255, Imgproc.THRESH_BINARY);
        //Core.bitwise_xor(Red_ch_upper, Red_ch_lower, Red_ch_upper);

        //chromatise and make binary the green channel
        Core.divide(Green_ch_upper, Sum_of_channels, Temp_mat);//chrome the green channel and store into complete temporarily
        Imgproc.threshold(Temp_mat, Green_ch_lower, green_lower_threshold, 255, Imgproc.THRESH_BINARY);//threshold everything above lower green chrome range
        Imgproc.threshold(Temp_mat, Green_ch_upper, green_upper_threshold, 255, Imgproc.THRESH_BINARY);//threshold everything above upper green chrome range
        //Core.bitwise_not(Green_ch_upper,Green_ch_upper);
        //Core.bitwise_and(Green_ch_upper, Green_ch_lower, Green_ch_upper);
        //Core.bitwise_xor(Green_ch_upper, Green_ch_lower, Green_ch_upper);//Get rid of pixels above upper green threshold, store in green_ch_upper

        Core.bitwise_xor(Green_ch_upper, Red_ch_lower, Complete);//Write to pixels that are within desired green range AND red range and store in Red_ch_lower

        //clean out the noise
        //for (int i = 0; i < 2; i++) {
            //Kernel.create(5+i*2,5+i*2,CvType.CV_8U);
        Imgproc.erode(Complete, Complete, Kernel);
        Imgproc.dilate(Complete, Complete, Kernel);
//        Imgproc.dilate(Complete,Complete,Kernel);
//        Imgproc.erode(Complete, Complete, Kernel_L);
//        Imgproc.dilate(Complete, Complete, Kernel_L);
//        Imgproc.dilate(Complete, Complete, Kernel_L);
//        Imgproc.erode(Complete, Complete, Kernel_L);


        Imgproc.HoughCircles(Complete, Circles_out, Imgproc.CV_HOUGH_GRADIENT, 2, 50, 100, 40, 150, 400);

        float circle[] = new float[3];

        for (int j=0; j<Circles_out.cols();j++ ){
            Circles_out.get(0,j,circle);
            if((int)circle[2]>radius){
                radius = (int)circle[2];
                Center.x = circle[0];
                Center.y = circle[1];
            }
        }
        Imgproc.circle(Complete, Center, radius, new Scalar(255, 0,0,255), 1);


        return Complete;
    }

//    public Mat HSV_select(Mat image){
//        /*
//        This function is used to select an HSV value to use for thresholding as opposed to
//        an RGB value.
//         */
//        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2HSV);
//        Core.extractChannel(image, Complete,0);//Extract the hue channel to the Matrix "complete"
//
//        temp = blobColourHSV.val[0];
//        //This part applies the threshold to the hue values of the image.
//        if(blobColourHSV.val[0]-15>0){
//            Imgproc.threshold(Complete, Red_ch_lower,(blobColourHSV.val[0]-15), 255, Imgproc.THRESH_BINARY);
//        }
//        else{
//            Imgproc.threshold(Complete, Red_ch_lower,0, 255, Imgproc.THRESH_BINARY);
//        }
//        if((blobColourHSV.val[0]+15)<255){
//            Imgproc.threshold(Complete, Red_ch_upper,(blobColourHSV.val[0]+15), 255, Imgproc.THRESH_BINARY);
//        }
//        else{
//            Imgproc.threshold(Complete, Red_ch_upper,255, 255, Imgproc.THRESH_BINARY);
//        }
//        Core.bitwise_xor(Red_ch_upper, Red_ch_lower, Complete);
//
//        //
//
//        return Complete;
//    }


//    public Mat Threshold_select(Mat image){
//        //This function thresholds the image for the selected colour (from the touched region)
//
//        //extract red and green channels
//        Core.extractChannel(image, Red_ch_upper, 0);
//        Core.extractChannel(image, Green_ch_upper, 1);
//
//        //Threshold upper and lower red channel limits
//        if (chrome_colour_r-red_col_tol>0) {
//            Imgproc.threshold(Red_ch_upper, Red_ch_lower, chrome_colour_r - red_col_tol+20, 255, Imgproc.THRESH_BINARY);
//        }
//        else{
//            Imgproc.threshold(Red_ch_upper, Red_ch_lower, 0, 255, Imgproc.THRESH_BINARY);
//        }
//        if(chrome_colour_r+red_col_tol>255){
//            Imgproc.threshold(Red_ch_upper, Red_ch_upper, 255, 255, Imgproc.THRESH_BINARY);
//        }
//        else{
//            Imgproc.threshold(Red_ch_upper, Red_ch_upper, chrome_colour_r + red_col_tol, 255, Imgproc.THRESH_BINARY);
//        }
//        Core.bitwise_xor(Red_ch_upper, Red_ch_lower, Red_ch_upper);
//        //Extract Green
//        if (chrome_colour_g-green_col_tol>0) {
//            Imgproc.threshold(Green_ch_upper, Green_ch_lower, chrome_colour_g - red_col_tol, 255, Imgproc.THRESH_BINARY);
//        }
//        else{
//            Imgproc.threshold(Green_ch_upper, Green_ch_lower, 0, 255, Imgproc.THRESH_BINARY);
//        }
//        if(chrome_colour_g+green_col_tol>255){
//            Imgproc.threshold(Green_ch_upper, Green_ch_upper, 255, 255, Imgproc.THRESH_BINARY);
//        }
//        else{
//            Imgproc.threshold(Green_ch_upper, Green_ch_upper, chrome_colour_g + red_col_tol, 255, Imgproc.THRESH_BINARY);
//        }
//
//        Core.bitwise_xor(Green_ch_upper, Green_ch_lower, Green_ch_upper);
//
//        Core.bitwise_and(Red_ch_upper, Green_ch_upper, Complete);
//        //Todo: Apply smoothing operations. Erode, dialate, blur
//
//        return Complete;
//    }

    public Scalar Channel_select_ball(Mat colour_sampleRGB, Rect touched_region){
        /*
        This function will take the colour of the selected region  and transform it to a scalar
        value to use for image processing and thresholding
         */
        //int temp;
        Mat colour_sampleHSV = new Mat();
        Imgproc.cvtColor(colour_sampleRGB, colour_sampleHSV, Imgproc.COLOR_RGB2HSV);
        blobColourHSV = Core.sumElems(colour_sampleHSV);
        int pixels_in_region = touched_region.width * touched_region.height;

        for (int i = 0; i<blobColourHSV.val.length; i++){
            //temp = blobColourHSV.val[i];
            blobColourHSV.val[i]/=pixels_in_region;
        }

        blobColorRGB = convertHSV2RGB(blobColourHSV);

        chrome_colour_y = blobColorRGB.val[0]+blobColorRGB.val[1]+blobColorRGB.val[2];
        chrome_colour_r = blobColorRGB.val[0]/chrome_colour_y;
        chrome_colour_g = blobColorRGB.val[1]/chrome_colour_y;

        //ProcFlag = false;



        return blobColorRGB;
    }




    private Scalar convertHSV2RGB(Scalar hsvColor){
        //Mat pointMatRgba = new Mat();
        Mat pointMatConvert = new Mat(1,1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatConvert,pointMatConvert,Imgproc.COLOR_HSV2RGB_FULL);
        return new Scalar(pointMatConvert.get(0,0));
    }

}
