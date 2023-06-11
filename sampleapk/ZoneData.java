package jp.jaxa.iss.kibo.rpc.sampleapk;

import java.util.*;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

public class ZoneData {
    public static double[] outXMin = {10,783, 10.8652, 10.185, 10.7955, 10.563};
    public static double inXMin = 10.3;
    public static double[] outYMin = {-9.8899, -9.0734, -8.3826, -8.0635, -7.1449};
    public static double inYMin = -10.2;
    public static double[] outZMin = {4.8385, 4.3861, 4.1475, 5.1055, 4.6544};
    public static double inZMin = 4.32;
    public static double[] outXMax = {11.071, 10.9628, 11.665, 11.3525, 10.709};
    public static double inXMax = 11.55;
    public static double[] outYMax = {-9.6929, -8.7314, -8.2826, -7.7305, -6.8099};
    public static double inYMax = -6.0;
    public static double[] outZMax = {5.0665, 4.6401, 4.6725, 5.1305, 4.8164};
    public static double inZMax = 5.57;
    public static final double AVOIDANCE = 0.4;

    public static List<moveData> intermediateData(moveData endData) {
        List<moveData> output = new ArrayList<moveData>();

        moveData currentData = new moveData();
        boolean leftToRight = currentData.point.getY() < endData.point.getY();

        if(leftToRight) {
            for(int i = 0; i < outYMin.length; i++) {
                if(outYMin[i] <= endData.point.getY()) {
                    output.add(new moveData(new Point(currentData.point.getX(), outYMin[i] - AVOIDANCE, outZMax[i] + AVOIDANCE), new Quaternion(0f, 0.707f, 0f, -0.707f)));
                    output.add(new moveData(new Point(currentData.point.getX(), outYMax[i] + AVOIDANCE, outZMax[i] + AVOIDANCE), new Quaternion(0f, 0.707f, 0f, -0.707f)));
                }
            }
        } else {
            for(int i = 0; i < outYMax.length; i++) {
                if(outYMax[i] > endData.point.getY()) {
                    if(outYMax[i] >= endData.point.getY()) {
                        output.add(new moveData(new Point(currentData.point.getX(), outYMax[i] + AVOIDANCE, outZMax[i] + AVOIDANCE), new Quaternion(0f, 0.707f, 0f, 0.707f)));
                        output.add(new moveData(new Point(currentData.point.getX(), outYMin[i] - AVOIDANCE, outZMax[i] + AVOIDANCE), new Quaternion(0f, 0.707f, 0f, 0.707f)));
                    }
                }
            }
        }

        return output;
    }
}