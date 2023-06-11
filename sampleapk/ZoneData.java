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

    //Returns a list of data points between the current position and a given end position in order to avoid hitting KOZ
    public static List<moveData> intermediateData(moveData endData) {
        //The list of points to be returned
        List<moveData> output = new ArrayList<moveData>();

        //The current position
        moveData currentData = new moveData();

        //For-each KOZ
        for(int i = 0; i < outYMin.length; i++) {
            //Updates currentData
            if(output.size() > 0) {
                currentData = output.get(output.size() - 1);
            }

            //Checks if Astrobee passes by/through the KOZ
            if((currentData.point.getY() <= outYMin[i] && endData.point.getY() >= outYMax[i]) || (currentData.point.getY() >= outYMax[i] && endData.point.getY() <= outYMin[i])) {
                /*//Gets the slope with respect to different variables, so we can better pathfind
                double  dydx = dydx(currentData, endData),
                        dzdx = dzdx(currentData, endData),
                        dxdy = dxdy(currentData, endData),
                        dzdy = dzdy(currentData, endData),
                        dxdz = dxdz(currentData, endData),
                        dydz = dydz(currentData, endData);

                //Sum of slopes that are with respect to the given variables
                double xSum = dydx + dzdx;
                double ySum = dxdy + dzdy;
                double zSum = dxdz + dydz;*/
                //Whether or not Astrobee is moving left to right (example: Bay 7 to Bay 6)
                boolean leftToRight = Math.abs(currentData.point.getY() - outYMin[i]) < Math.abs(currentData.point.getY() - outYMax[i]);
                //Whether or not Astrobee is moving down to up (example: floor to ceiling) {
                boolean downToUp = Math.abs(currentData.point.getZ() - outZMax[i]) < Math.abs(currentData.point.getZ() - outZMin[i]);
                //Whether or not Astrobee is moving backwards to forwards (example: docking zone to the big KIZ)
                boolean backToFront = Math.abs(currentData.point.getX() - outXMax[i]) < Math.abs(currentData.point.getX() - outXMin[i]);

                //The least distance using the computed path
                double leastDistance = Double.MAX_VALUE;
                //The two sets of coordinates that correspond to the path of least distance
                double x1 = Double.MAX_VALUE, y1 = Double.MAX_VALUE, z1 = Double.MAX_VALUE, x2 = Double.MAX_VALUE, y2 = Double.MAX_VALUE, z2 = Double.MAX_VALUE;
                //The values for the quaternion (x, y, z, w)
                float q1 = 1, q2 = 0, q3 = 0, q4 = 0;
                //The lines formed by avoiding the KOZ on either side for each variable
                double xFirst = backToFront ? outXMax[i] + AVOIDANCE : outXMin[i] - AVOIDANCE;
                double xSecond = backToFront ? outXMin[i] - AVOIDANCE : outXMax[i] + AVOIDANCE;
                double yFirst = leftToRight ? outYMin[i] - AVOIDANCE : outYMax[i] + AVOIDANCE;
                double ySecond = leftToRight ? outYMax[i] + AVOIDANCE : outYMin[i] - AVOIDANCE;
                double zFirst = downToUp ? outZMax[i] + AVOIDANCE : outZMin[i] - AVOIDANCE;
                double zSecond = downToUp ? outZMin[i] - AVOIDANCE : outZMax[i] + AVOIDANCE;

                //The number of iterations for the loops to go through (remember, it will do steps^2 iterations)
                int steps = 25;

                //Checks whether it even makes sense to calculate while holding x constant
                if(xFirst >= inXMin && xFirst <= inXMax && xSecond >= inXMin && xSecond <= inXMax) {
                    for(double y = inYMin; y <= inYMax; y += (inYMax - inYMin) / steps) {
                        for(double z = inZMin; z <= inZMax; z += (inZMax - inZMin) / steps) {
                            double temp = distance(xFirst, y, z, currentData) + distance(xSecond, y, z, endData);

                            if(temp < leastDistance) {
                                leastDistance = temp;
                                x1 = xFirst;
                                y1 = y;
                                z1 = z;
                                x2 = xSecond;
                                y2 = y;
                                z2 = z;

                                if(backToFront) {
                                    q1 = 1f;
                                    q2 = 0f;
                                    q3 = 0f;
                                    q4 = 0f;
                                } else {
                                    q1 = 0f;
                                    q2 = 0.707f;
                                    q3 = 0.707f;
                                    q4 = 0f;
                                }
                            }
                        }
                    }
                }
                if(yFirst >= inYMin && yFirst <= inYMax && ySecond >= inYMin && ySecond <= inYMax) {
                    for(double x = inXMin; x <= inXMax; x += (inXMax - inXMin) / steps) {
                        for(double z = inZMin; z <= inZMax; z += (inZMax - inZMin) / steps) {
                            double temp = distance(x, yFirst, z, currentData) + distance(x, ySecond, z, endData);

                            if(temp < leastDistance) {
                                leastDistance = temp;
                                x1 = x;
                                y1 = yFirst;
                                z1 = z;
                                x2 = x;
                                y2 = ySecond;
                                z2 = z;

                                if(leftToRight) {
                                    q1 = 0f;
                                    q2 = 0.707f;
                                    q3 = 0f;
                                    q4 = -0.707f;
                                } else {
                                    q1 = 0f;
                                    q2 = 0.707f;
                                    q3 = 0f;
                                    q4 = 0.707f;
                                }
                            }
                        }
                    }
                }
                if(zFirst >= inZMin && zFirst <= inZMax && zSecond >= inZMin && zSecond <= inZMax) {
                    for(double x = inXMin; x <= inXMax; x += (inXMax - inXMin) / steps) {
                        for(double y = inYMin; y <= inYMax; y += (inYMax - inYMin) / steps) {
                            double temp = distance(x, y, zFirst, currentData) + distance(x, y, zSecond, endData);

                            if(temp < leastDistance) {
                                leastDistance = temp;
                                x1 = x;
                                y1 = y;
                                z1 = zFirst;
                                x2 = x;
                                y2 = y;
                                z2 = zSecond;

                                if(downToUp) {
                                    q1 = 0f;
                                    q2 = 0f;
                                    q3 = 0.707f;
                                    q4 = 0.707f;
                                } else {
                                    q1 = 0f;
                                    q2 = 0f;
                                    q3 = 0.707f;
                                    q4 = -0.707f;
                                }
                            }
                        }
                    }
                }

                output.add(new moveData(new Point(x1, y1, z1), new Quaternion(q1, q2, q3, q4)));
                output.add(new moveData(new Point(x2, y2, z2), new Quaternion(q1, q2, q3, q4)));
            }
        }

        return output;
    }

    //Returns the change in y in relation to the change in x of two moveData (assumes a constant slope)
    private static double dydx(moveData start, moveData end) {
        return (end.point.getY() - start.point.getY()) / (end.point.getX() / start.point.getX());
    }
    //Above but with the change in z in relation to the change in x
    private static double dzdx(moveData start, moveData end) {
        return (end.point.getY() - start.point.getY()) / (end.point.getX() / start.point.getX());
    }
    //x in relation to the change in y
    private static double dxdy(moveData start, moveData end) {
        return 1 / dydx(start, end);
    }
    //z in relation to the change in y
    private static double dzdy(moveData start, moveData end) {
        return dzdx(start, end) * dxdy(start, end);
    }
    //x in relation to z
    private static double dxdz(moveData start, moveData end) {
        return 1 / dzdx(start, end);
    }
    //y in relation to z
    private static double dydz(moveData start, moveData end) {
        return 1 / dzdy(start, end);
    }

    //Returns the distance between the current point and the point (x,y,z)
    private static double distance(double x, double y, double z, moveData current) {
        return Math.sqrt(Math.pow(current.point.getX() - x, 2) + Math.pow(current.point.getY() - y, 2) + Math.pow(current.point.getZ() - z, 2));
    }

    //Returns whether or not the point (x,y,z) is in the big KIZ
    public static boolean inBounds(double x, double y, double z) {
        return x <= inXMax && x >= inXMin && y <= inYMax && y >= inYMin && z <= inZMax && z >= inZMin;
    }
}
