package jp.jaxa.iss.kibo.rpc.sampleapk;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

public class TargetData {
    //The intial position of all the targets, + the qr code, + the goal
    public static moveData[] intial = initialize();
    //The updated position of all the targets, + the qr code, + the goal (updateTarget changes it)
    public static moveData[] updated = initialize();

    //All the moveData representations of the intial values of the targets and whatnot
    public static moveData[] initialize() {
        return new moveData[]{new moveData(new Point(11.2746, -9.92284, 5.2988), new Quaternion(0f, 0f, -0.707f, 0.707f)),
            new moveData(new Point(10.612, -9.0709, 4.48), new Quaternion(0.5f, 0.5f, -0.5f, 0.5f)),
            new moveData(new Point(10.71, -7.7, 4.48), new Quaternion(0f, 0.707f, 0f, 0.707f)),
            new moveData(new Point(10.51, -6.7185, 5.1804), new Quaternion(0f, 0f, -1f, 0f)),
            new moveData(new Point(11.114, -7.9756, 5.3393), new Quaternion(-0.5f, -0.5f, -0.5f, 0.5f)),
            new moveData(new Point(11.355, -8.9929, 4.7818), new Quaternion(0f, 0f, 0f, 1f)),
            new moveData(new Point(11.381944, -8.566172, 4.5), new Quaternion(0f, 1f, 0f, 0f)), //Actual z of qr is 3.76203, but that's out of bounds
            new moveData(new Point(11.143, -6.7607, 4.9654), new Quaternion(0f, 0f, -0.707f, 0.707f))};
    }

    //Updates target data using current position as well as the target number (1-8)
    public static void updateTarget(int targetNum) {
        updated[targetNum - 1] = new moveData();
    }
}
