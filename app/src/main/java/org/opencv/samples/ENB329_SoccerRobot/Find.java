package org.opencv.samples.ENB329_SoccerRobot;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raphael on 5/08/15.
 */
public class Find {


//    public Find(){
//
//    }


    public Mat Channel_remove(Mat Sauce) {
        /*
        This is to make channel selection easier.
        Assumes that the mat type is 4 channel rgba
        //Todo add a definition table for these values
        RGBA:
        1 = R
        2 = G
        3 = B
        4 = RG
        5 = RB
        6 = GB
         */
        List Mat_ch = new ArrayList();

        Mat Blue_ch = new Mat(Sauce.size(), CvType.CV_8UC1);
        Mat Green_ch = new Mat(Sauce.size(), CvType.CV_8UC1);
        Mat Red_ch = new Mat(Sauce.size(), CvType.CV_8UC1);
        //Mat Alpha_ch = new Mat();
        Mat Complete = new Mat(Sauce.size(), CvType.CV_8UC1);

        Mat_ch.add(Blue_ch);
        Mat_ch.add(Green_ch);
        Mat_ch.add(Red_ch);
        //Mat_ch.add(Alpha_ch);

        Imgproc.cvtColor(Sauce, Complete, Imgproc.COLOR_RGBA2RGB);
        Core.split(Complete, Mat_ch);
        Mat_ch.remove(1); //removes the chosen channel

        //Mat_ch.remove(4); //removes the alpha channel
        Blue_ch.zeros(Sauce.size(), CvType.CV_8UC1);
        Mat_ch.add(1, Blue_ch);
        Mat_ch.remove(2);
        Green_ch.zeros(Sauce.size(), CvType.CV_8UC1);
        Mat_ch.add(2, Green_ch);

        Core.merge(Mat_ch,Complete);

        Blue_ch.release();
        Green_ch.release();
        Red_ch.release();

        return Complete;
    }

}
