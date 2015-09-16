package org.opencv.samples.ENB329_SoccerRobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.widget.ListView;

import java.util.Set;

/**
 * Created by Raphael Cardona on 5/08/15.
 * This is the move class which is contains the methods necessary for motor control and movement
 */
public class Move extends Activity {

    private static final String ForTesting = "MoveFunctions::";

    //bluetooth stuff
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv;
    //End of bluetooth stuff

    int r = 0; //distance between the centre of the robot and the centre of the ball during contact

    public boolean Setup(){
        //This function is to set up the communication with the bluetooth device
        Log.i(ForTesting, "Setting up Bluetooth Communication");
        //Todo: Implement bluetooth module
        //Todo: Setup bluetooth communication
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null){
            //error case here, no bluetooth support
        }
        else{
            if (!mBluetoothAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,0);
            }
        }


        return false;
    }

    private void bluetoothVisible(){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible,0);
    }

    public boolean find_ball(){
        //The robot will spin around and do random shit until the ball comes in view
        Log.i(ForTesting, "Finding Ball");
        //Todo: Write function to make the robot spin slowly twice while looking for the ball
        //Todo: Write function to make the robot wander in a random direction for a random distance before spinning again

        return false;
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
