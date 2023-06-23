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
        return new moveData[]{new moveData(new Point(11.2746 - 0.067, -9.92284, 5.2988 + 0.174), new Quaternion(0f, 0f, -0.707f, 0.707f)),
                /*new moveData(new Point(10.612, -9.0709, 4.48), new Quaternion(0.5f, 0.5f, -0.5f, 0.5f)),*/
                new moveData(new Point(10.513384 + 0.107, -9.085172 - 0.0572, 4.5), new Quaternion(0f, 0.707f, 0f, 0.707f)),
                new moveData(new Point(10.71 + 0.003, -7.7 - 0.068, 4.48), new Quaternion(0f, 0.707f, 0f, 0.707f)),
                new moveData(new Point(10.51, -6.7185 + 0.104, 5.1804 + 0.027), new Quaternion(0f, 0f, -1f, 0f)),
                new moveData(new Point(11.114 - 0.068, -7.9756 + 0.059, 5.3393), new Quaternion(-0.5f, -0.5f, -0.5f, 0.5f)),
                new moveData(new Point(11.355, -8.9929 - 0.052, 4.7818 + 0.12), new Quaternion(0f, 0f, 0f, 1f)),
                new moveData(new Point(11.381944, -8.566172, 4.5), new Quaternion(0f, 0.707f, 0f, -0.707f)), //Actual z of qr is 3.76203, but that's out of bounds
                new moveData(new Point(11.143, -6.7607, 4.9654), new Quaternion(0f, 0f, -0.707f, 0.707f))};
    }

    //Updates target data using current position as well as the target number (1-8)
    public static void updateTarget(int targetNum) {
        updated[targetNum - 1] = new moveData();
    }
}
/* Actual values
return new moveData[]{new moveData(new Point(11.2746, -9.92284, 5.2988), new Quaternion(0f, 0f, -0.707f, 0.707f)),
            new moveData(new Point(10.612, -9.0709, 4.48), new Quaternion(0.5f, 0.5f, -0.5f, 0.5f)),
            new moveData(new Point(10.71, -7.7, 4.48), new Quaternion(0f, 0.707f, 0f, 0.707f)),
            new moveData(new Point(10.51, -6.7185, 5.1804), new Quaternion(0f, 0f, -1f, 0f)),
            new moveData(new Point(11.114, -7.9756, 5.3393), new Quaternion(-0.5f, -0.5f, -0.5f, 0.5f)),
            new moveData(new Point(11.355, -8.9929, 4.7818), new Quaternion(0f, 0f, 0f, 1f)),
            new moveData(new Point(11.381944, -8.566172, 4.5), new Quaternion(0f, 0.707f, 0f, -0.707f)), //Actual z of qr is 3.76203, but that's out of bounds
            new moveData(new Point(11.143, -6.7607, 4.9654), new Quaternion(0f, 0f, -0.707f, 0.707f))};
 */
/*
return new moveData[]{new moveData(new Point(11.26, -9.91, 5.28), new Quaternion(0f, 0f, -0.707f, 0.707f)),
            new moveData(new Point(10.612, -9.0709, 4.7), new Quaternion(0.5f, 0.5f, -0.5f, 0.5f)),
            new moveData(new Point(10.71, -7.7, 4.7), new Quaternion(0f, 0.707f, 0f, 0.707f)),
            new moveData(new Point(10.59, -6.7185, 5.1804), new Quaternion(0f, 0f, -1f, 0f)),
            new moveData(new Point(11.114, -7.9756, 5.28), new Quaternion(-0.5f, -0.5f, -0.5f, 0.5f)),
            new moveData(new Point(11.26, -8.9929, 4.7818), new Quaternion(0f, 0f, 0f, 1f)),
            new moveData(new Point(11.26, -8.566172, 4.7), new Quaternion(0f, 0.707f, 0f, -0.707f)), //Actual z of qr is 3.76203, but that's out of bounds
            new moveData(new Point(11.143, -6.7607, 4.9654), new Quaternion(0f, 0f, -0.707f, 0.707f))};
 */
