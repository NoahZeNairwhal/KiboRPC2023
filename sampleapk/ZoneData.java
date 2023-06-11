package jp.jaxa.iss.kibo.rpc.sampleapk;

import java.util.*;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

public class ZoneData {
    //The minimum X bounds of the KOZs
    public static double[] outXMin = {10.783, 10.8652, 10.185, 10.7955, 10.563};
    //The minimum X bound of the big KIZ
    public static double inXMin = 10.3;
    //Minimum Y bounds of KOZs
    public static double[] outYMin = {-9.8899, -9.0734, -8.3826, -8.0635, -7.1449};
    //Minimum Y bound of big KIZ
    public static double inYMin = -10.2;
    //Minimum Z bounds of KOZs
    public static double[] outZMin = {4.8385, 4.3861, 4.1475, 5.1055, 4.6544};
    //Minimum Z bound of big KIZ
    public static double inZMin = 4.32;
    //Maximum X bounds of KOZs
    public static double[] outXMax = {11.071, 10.9628, 11.665, 11.3525, 10.709};
    //Maximum X bound of big KIZ
    public static double inXMax = 11.55;
    //Maximum Y bounds of KOZs
    public static double[] outYMax = {-9.6929, -8.7314, -8.2826, -7.7305, -6.8099};
    //Maximum Y bound of big KIZ
    public static double inYMax = -6.0;
    //Maximum Z bounds of KOZs
    public static double[] outZMax = {5.0665, 4.6401, 4.6725, 5.1305, 4.8164};
    //Maximum Z bound of big KIZ
    public static double inZMax = 5.57;
    //Meant to take into account the size of Astrobee and the randomness of the environment to avoid hitting the edges of the KOZ while pathfinding
    public static final double AVOIDANCE = 0.4;

    //Returns a list of data points between the current poisition and a given end position in order to avoid hitting KOZ
    public static List<moveData> intermediateData(moveData endData) {
        //The list of points to be returned
        List<moveData> output = new ArrayList<moveData>();

        //The current position
        moveData currentData = new moveData();
        //Stores whether the Astrobee is moving from left to right (example: moving from bay 7 to bay 6)
        boolean leftToRight = currentData.point.getY() < endData.point.getY();

        if(leftToRight) {
            //For every minimum y bound of the KOZs
            for(int i = 0; i < outYMin.length; i++) {
                //If the y bound is less than the end point (that is, if Astrobee passes through it)
                if(outYMin[i] <= endData.point.getY()) {
                    //Add two points, one before and below the KOZ, and another after and below the KOZ
                    output.add(new moveData(new Point(currentData.point.getX(), outYMin[i] - AVOIDANCE, outZMax[i] + AVOIDANCE), new Quaternion(0f, 0.707f, 0f, -0.707f)));
                    output.add(new moveData(new Point(currentData.point.getX(), outYMax[i] + AVOIDANCE, outZMax[i] + AVOIDANCE), new Quaternion(0f, 0.707f, 0f, -0.707f)));
                }
            }
        } else {
            //For every maximum y bound of the KOZs
            for(int i = 0; i < outYMax.length; i++) {
                //If the y bound is greater than the end point (Astrobee passes through it)
                if(outYMax[i] >= endData.point.getY()) {
                    //Add two points, one before and below the KOZ, and another after and below the KOZ
                    output.add(new moveData(new Point(currentData.point.getX(), outYMax[i] + AVOIDANCE, outZMax[i] + AVOIDANCE), new Quaternion(0f, 0.707f, 0f, 0.707f)));
                    output.add(new moveData(new Point(currentData.point.getX(), outYMin[i] - AVOIDANCE, outZMax[i] + AVOIDANCE), new Quaternion(0f, 0.707f, 0f, 0.707f)));
                }
            }
        }
        
        return output;
    }
}
