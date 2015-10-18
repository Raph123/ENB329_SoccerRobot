package org.opencv.samples.ENB329_SoccerRobot;

import android.app.Activity;
import android.util.Log;

import org.opencv.core.Point;

/**
 * Created by Raphael Cardona on 5/08/15.
 * This is the move class which is contains the methods necessary for motor control and movement
 */
public class Move extends Activity {

    private static final String ForTesting = "MoveFunctions::";
    private char[] testArray;
    public Move(){
        testArray = new char[] {0,0,0};

    }

    int r = 0; //distance between the centre of the robot and the centre of the ball during contact

    public boolean Setup(){
        //This function is to set up the communication with the bluetooth device
//        Log.i(ForTesting, "Setting up Bluetooth Communication");
//
//        else{
//            if (!mBluetoothAdapter.isEnabled()){
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent,1);
//            }
//        }
//        Set<BluetoothDevice> pairedDevides = mBluetoothAdapter.getBondedDevices();
//        if (pairedDevices.size()>0){
//            for (BluetoothDevice device : pairedDevices){
//                mDevice = device;
//            }
//        }


        return false;
    }

//
//    private void bluetoothVisible(){
//        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        startActivityForResult(getVisible,0);
//    }

    public char[] find_ball( int radius, Point ballLoc, org.opencv.core.Rect rect, Point wallP1, Point wallP2, boolean dribblingFlag){
        //The robot will spin around and do random shit until the ball comes in view

        Log.i(ForTesting, "Finding Ball");
        //Todo: Write function to make the robot spin slowly twice while looking for the ball
        //Todo: Write function to make the robot wander in a random direction for a random distance before spinning again
        testArray[0] = 0;
        testArray[1] = 0;
        testArray[2] = 0;
        if (radius <= 2){
            //if no ball found, spin around
            if(dribblingFlag){
                testArray[1] = 's';
                testArray[0] = 'Y';
                testArray[2] = 'Y';
            }
            else{
                testArray[1] = 'Y';
                testArray[0] = 'Y';
                testArray[2] = 's';
            }

        }
        else{
            //Ball movements
            //if (dribblingFlag) {
                testArray[1] = 's';
                //if we're dribbling
                if ((ballLoc.y > 172) && (ballLoc.y < 287)) {
                    testArray[1] = 's';
                    testArray[0] = 0;
                    testArray[2] = 0;
                    Log.i(ForTesting, "Going forward");
                    //command to move forward
                } else if ((ballLoc.y > 115) && (ballLoc.y < 345)) {
                    testArray[1] = 's';
                    if (ballLoc.y < 173) {
                        testArray[2] = 's';
                    } else if (ballLoc.y > 286) {
                        testArray[2] = 'c';
                    }
                    testArray[0] = 0;

                    Log.i(ForTesting, "Going forward");
                    //command to move forward
                } else {
                    if (ballLoc.y < 115) {
                        //testArray[2] = 's';
                        Log.i(ForTesting, "rotating left");
                        testArray[2] = 's';
                        // rotate left
                    } else if (ballLoc.y > 344) {
                        testArray[2] = 'c';
                        Log.i(ForTesting, "rotating right");
                        //rotate right
                    }
                    testArray[0] = 0;
                }
 //           }
//            else{
//                //when we're not dribbling...
//                //testArray[1] = 's';
//                if ((ballLoc.y > 172) && (ballLoc.y < 287)) {
//                    testArray[1] = 's';
//                    testArray[0] = 0;
//                    testArray[2] = 0;
//                    Log.i(ForTesting, "Going forward");
//                    //command to move forward
//                } else if ((ballLoc.y > 115) && (ballLoc.y < 345)) {
//                    testArray[1] = 's';
//                    if (ballLoc.y < 173) {
//                        testArray[2] = 's';
//                    } else if (ballLoc.y > 286) {
//                        testArray[2] = 'c';
//                    }
//                    testArray[0] = 0;
//
//                    Log.i(ForTesting, "Going forward");
//                    //command to move forward
//                } else {
//                    testArray[1] = 's';
//                    if (ballLoc.y < 115) {
//                        //testArray[2] = 's';
//                        Log.i(ForTesting, "rotating left");
//                        testArray[2] = 's';
//                        // rotate left
//                    } else if (ballLoc.y > 344) {
//                        testArray[2] = 'c';
//                        Log.i(ForTesting, "rotating right");
//                        //rotate right
//                    }
//                    testArray[0] = 0;
//                }
//
            }
            //Obstacle movements
            if (rect.x < 30){
                if (rect.width > 400){
                    testArray[0] = 0;
                    testArray[1] = 0;
                    testArray[2] = 's';
                }
                else if(((rect.y+rect.height)/2)<211){
                    //move right fast
                    testArray[0] = 'y';

                    //testArray[2] = 0;
                }
                else if(((rect.y+rect.height)/2)>210){
                    //move left fast
                    testArray[0] = 's';

                    //testArray[2] = 0;
                }
//            if((rect.y + rect.height/2)>210){
//                testArray[0] = ';';
//            }
//            else{
//                testArray[0] = 's';
//            }

            }
            else if(rect.x < 60){
                //obstacle is between 10-15cm away 160
                if(rect.y > 210){
                    testArray[0] = ';';

                    //move left
                }
                else if((rect.y+rect.height)<210){
                    //move right
                    testArray[0] = 's';
                }
                else if(((rect.y+rect.height)/2)>210){
                    //move left fast
                    testArray[0] = ';';
                }
                else if(((rect.y+rect.height)/2)<211){
                    //move right fast
                    testArray[0] = 'y';
                }

            }
            else if(rect.x < 240){
                //obstacle is between 15 and 20
                if(((rect.y+rect.height)/2)>210){
                    //move left fast
                    testArray[0] = 'l';
                }
                else if(((rect.y+rect.height)/2)<211){
                    //move right fast
                    testArray[0] = 'r';
                }

            }
            else if(rect.x < 320){
                //obstacle is between 20 and 30
                testArray[0] = ';';
           }
//        }
        // wall movements
        if ((wallP1.x < 30)&& (wallP2.x < 30)){
            testArray[0] = 'Y';
            testArray[1] = 'Y';
            if (ballLoc.y>210){
                //rotate left
                testArray[2] = 'c';
            }
            else{
                //rotate right
                testArray[2] = 's';
            }
        }
//        else if ((wallP1.y>210)&&(wallP2.y>210)){
//            //wall is to the left or the robot
//            //if((wallP1.x < 200)&&(wallP2.x < 200)){
//                double gradient;
//                gradient = Math.abs((wallP1.x-wallP2.x)/(wallP1.y - wallP2.y));
//                if (gradient < 1){
//                    testArray[1] = 'Y';
//                    testArray[2] = 's';
//                    testArray[0] = 'Y';
//                }
//                if (gradient >1){
//                    testArray[1] = 'Y';
//                    testArray[2] = 'Y';
//                    testArray[0] = 115;
//                }
//            //}
//
//
//
//        }
//        else if ((wallP1.y<210)&&(wallP2.y<210)) {
//            //wall is to the right or the robot
//            double gradient;
//            gradient = Math.abs((wallP1.x - wallP2.x) / (wallP1.y - wallP2.y));
//            if (gradient < 1) {
//                testArray[1] = 'Y';
//                testArray[2] = 'c';
//                testArray[0] = 'Y';
//            }
//            if (gradient > 1) {
//                testArray[1] = 'Y';
//                testArray[2] = 'Y';
//                testArray[0] = 59;
//            }
//        }
//        else if (((wallP1.y)>210)&&((wallP2.y)<210)||((wallP1.y)>210)&&((wallP2.y)<210)){
////            double gradient;
////            gradient = Math.abs((wallP1.x - wallP2.x) / (wallP1.y - wallP2.y));
//
//            testArray[1] = 'Y';
//            testArray[2] = 'c';
//            testArray[0] = 'Y';
//
//        }

        return testArray;
    }

    public void chase_ball(int ball_dist, int angle){
        Log.i(ForTesting, "Chasing Ball");
        //This function sets the magnitude and direction of the speed of the robot
        //The first arguement is the estimated distance from the ball
        //The second arguement is the estimated angle the ball is in relation to the robot
        //Todo: formula to find the best speed given the estimated distance of the ball
        //Todo: Formula to set motors to appropriate speed given direction
        //Todo: Send necessary information to motors via bluetooth

    }

    public void find_goal(){
        //This function is to find the goal. The robot will spin around to look for the goal posts
        //and do everything it did to find the ball, this time with the ball as the point of reference
    }

    public void control_ball(int goal_dist, int angle){
        //This function is to control the ball and the robot.
        //NOTE: This will involve moving relative to the ball (time to put dynamics to use!)
        //First argument is the estimated distance we are from the goal (Might not need this)
        //Second argument is the angle/ direction of the goal, relative to the robot
        Log.i(ForTesting, "Dribbling the ball!");
    }

}
