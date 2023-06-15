package jp.jaxa.iss.kibo.rpc.sampleapk;

import org.opencv.core.Mat;

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
    public static final double AVOIDANCE = 0.24;
    //Number of steps to use for the array below
    public static final int STEPS = 20;
    //A preset 4 dimensional array of possible points to use
    public static final double[][][][] MASTER_POINTS = masterPoints_init();

    private static double[][][][] masterPoints_init() {
        double[][][][] output = new double[STEPS][STEPS][STEPS][3];
        double x = inXMin + AVOIDANCE, y = inYMin + AVOIDANCE, z = inZMin + AVOIDANCE;

        for(int r = 0; r < output.length; r++) {
            for(int c = 0; c < output.length; c++) {
                for(int d = 0; d < output.length; d++) {
                    output[r][c][d][0] = x + (((inXMax - AVOIDANCE) - (inXMin + AVOIDANCE)) / (STEPS - 1)) * r;
                    output[r][c][d][1] = y + (((inYMax - AVOIDANCE) - (inYMin + AVOIDANCE)) / (STEPS - 1)) * c;
                    output[r][c][d][2] = z + (((inZMax - AVOIDANCE) - (inZMin + AVOIDANCE)) / (STEPS - 1)) * d;
                }
            }
        }

        return output;
    }

    //Returns a list of data points between the current position and a given end position in order to avoid hitting KOZ
    public static List<moveData> intermediateData(final moveData endData) {
        //The list of points to be returned
        List<moveData> output = new ArrayList<moveData>();
        //The current position
        moveData currentData = new moveData();
        //The KOZs to check
        final List<Integer> possibleZones = couldCross(endData.point.getY(), currentData);
        //The indexes of the points in the MASTER_POINTS array that would create a x/y/z boundary just outside of the current and end points
        int[][] name = indexWithMove(currentData, endData);
        //Needs to be final so Node and tree can access them
        final int[] currBounds = name[0];
        final int[] endBounds = name[1];

        //A Node class for the Tree
        class Node {
            //If this Node is within MASTER_POINTS, then each of this represent the x index, y index, and z index respectively
            int xBound;
            int yBound;
            int zBound;
            //A double array containing the actual point data, listed in the order x, y, z
            double[] points;
            //The total distance from the starting point to this Node
            double totDistance;
            //The List of Nodes stemming from this Node, as calculated in calcNext()
            List<Node> myNext;
            //This Node's parent node (which Node you could find it in myNext)
            Node parent;
            //A moveData representation of this Node, using the endData for the quaternion
            moveData data;

            //This constructor is used for the head of the Tree, AKA the currentData. Note the bounds are not used in this Node.
            Node(double[] points) {
                this.points = points;
                totDistance = 0;
                parent = null;
                myNext = new ArrayList<Node>();
                data = toMoveData();
            }

            //Used for child Nodes, this Node will be a point in the MASTER_POINTS array
            Node(int xIndex, int yIndex, int zIndex, double totDistance, Node parent) {
                xBound = xIndex;
                yBound = yIndex;
                zBound = zIndex;
                points = MASTER_POINTS[xIndex][yIndex][zIndex];
                this.totDistance = totDistance;
                this.parent = parent;
                myNext = new ArrayList<Node>();
                data = toMoveData();
            }

            //Calculates the List of next Nodes
            void calcNext(boolean first) {
                //Used to determine whether the r, c, d values of the MASTER_POINTS array traversal should increase or decrease
                boolean lessX = points[0] <= endData.point.getX();
                boolean lessY = points[1] <= endData.point.getY();
                boolean lessZ = points[2] <= endData.point.getZ();

                //Since the head Node won't have an x/y/z Bound, it needs a seperate case
                if(first) {
                    for(int r = 0; r < MASTER_POINTS.length; r++) {
                        for(int c = currBounds[1]; lessY ? c <= endBounds[1] : c >= endBounds[1]; c += lessY ? 1 : -1) {
                            for(int d = 0; d < MASTER_POINTS.length; d++) {
                                boolean crosses = false;

                                //If from the current Node to the possible new point it ends up touching a KOZ, then we can discard the new point
                                for(int i: possibleZones) {
                                    if(touchesZone(MASTER_POINTS[r][c][d][0], MASTER_POINTS[r][c][d][1], MASTER_POINTS[r][c][d][2], i, data)) {
                                        crosses = true;
                                        break;
                                    }
                                }

                                //Adds a new Node if it's good
                                if(!crosses) {
                                    myNext.add(new Node(r, c, d, totDistance + distance(MASTER_POINTS[r][c][d][0], MASTER_POINTS[r][c][d][1], MASTER_POINTS[r][c][d][2], data), this));
                                }
                            }
                        }
                    }
                } else {
                    //The use of bounds helps decrease the amount of computing done. Otherwise it could take a long time to find a path
                    for(int r = xBound; lessX ? r <= endBounds[0] : r >= endBounds[0]; r += lessX ? 1 : -1) {
                        for(int c = yBound; lessY ? c <= endBounds[1] : c >= endBounds[1]; c += lessY ? 1 : -1) {
                            for(int d = zBound; lessZ ? d <= endBounds[2] : d >= endBounds[2]; d += lessZ ? 1 : -1) {
                                boolean crosses = false;

                                for(int i: possibleZones) {
                                    if(touchesZone(MASTER_POINTS[r][c][d][0], MASTER_POINTS[r][c][d][1], MASTER_POINTS[r][c][d][2], i, data)) {
                                        crosses = true;
                                        break;
                                    }
                                }

                                if(!crosses) {
                                    myNext.add(new Node(r, c, d, totDistance + distance(MASTER_POINTS[r][c][d][0], MASTER_POINTS[r][c][d][1], MASTER_POINTS[r][c][d][2], data), this));
                                }
                            }
                        }
                    }
                }
            }

            //moveData representation of the Node utilizing endData's quaternion
            moveData toMoveData() {
                return new moveData(new Point(points[0], points[1], points[2]), endData.quaternion);
            }
        }
        class Tree {
            Node head;
            //The list of the ends of the Tree, that is, Nodes that don't have their next Nodes calculated yet
            List<Node> lowest;

            Tree(Node head) {
                this.head = head;
                lowest = new ArrayList<Node>();
                lowest.add(head);
            }

            //Calculates the next Nodes for every Node in lowest, and then updates lowest to be these new Nodes
            void calcNext() {
                List<Node> newLow = new ArrayList<Node>();

                for(Node aNode: lowest) {
                    if(aNode.equals(head)) {
                        aNode.calcNext(true);
                    } else {
                        aNode.calcNext(false);
                    }

                    newLow.addAll(aNode.myNext);
                }

                lowest = newLow;

                System.gc();
            }

            //Returns the Node with the lowest distance that can get from where it is to the end point without crossing a KOZ. If no Node works, it returns null
            Node best() {
                Node best = null;
                double leastDistance = Double.MAX_VALUE;

                for(Node low: lowest) {
                    boolean crosses = false;

                    for(int i: possibleZones) {
                        if(touchesZone(endData.point.getX(), endData.point.getY(), endData.point.getZ(), i, low.data)) {
                            crosses = true;
                            break;
                        }
                    }

                    if(!crosses) {
                        double tempDistance = low.totDistance + distance(low.points[0], low.points[1], low.points[2], endData);

                        if(tempDistance < leastDistance) {
                            best = low;
                            leastDistance = tempDistance;
                        }
                    }
                }

                return best;
            }

        }

        //Constructs a new Tree with the head as the current Data
        Tree wow = new Tree(new Node(new double[]{currentData.point.getX(), currentData.point.getY(), currentData.point.getZ()}));
        Node useThis = wow.best();

        //While there are no Nodes that can get to the end point
        while(useThis == null) {
            YourService.logger.info("Use this loop active");
            //Calculate another level
            wow.calcNext();
            //At some point best should return something other than null
            useThis = wow.best();

            System.gc();

            if(YourService.myApi.getTimeRemaining().get(1) <= 120000 && !YourService.bypass) {
                YourService.moveToGoal = true;
                return output;
            }
        }

        //Constructs the output List "backwards" by using the Node that worked and including every Node except for the head Node, since the head is the current position
        //Note this also excludes the end point
        for(Node temp = useThis; temp.parent != null; temp = temp.parent) {
            output.add(0, temp.data);
        }

        return output;
    }

    //Returns an array of the indexes for the "closest" point for the current moveData and the same for the end moveData
    public static int[][] indexWithMove(moveData current, moveData end) {
        int[] currentArr = new int[3];
        int[] endArr = new int[3];
        boolean lessX = current.point.getX() <= end.point.getX();
        boolean lessY = current.point.getY() <= end.point.getY();
        boolean lessZ = current.point.getZ() <= end.point.getZ();

        if(lessX) {
            for(int r = 0; r < MASTER_POINTS.length; r++) {
                if(MASTER_POINTS[r][0][0][0] >= end.point.getX()) {
                    endArr[0] = r;
                    break;
                }
            }

            for(int r = MASTER_POINTS.length - 1; r >= 0; r--) {
                if(MASTER_POINTS[r][0][0][0] <= current.point.getX()) {
                    currentArr[0] = r;
                    break;
                }
            }
        } else {
            for(int r = 0; r < MASTER_POINTS.length; r++) {
                if(MASTER_POINTS[r][0][0][0] >= current.point.getX()) {
                    currentArr[0] = r;
                    break;
                }
            }

            for(int r = MASTER_POINTS.length - 1; r >= 0; r--) {
                if(MASTER_POINTS[r][0][0][0] <= end.point.getX()) {
                    endArr[0] = r;
                    break;
                }
            }
        }

        if(lessY) {
            for(int c = 0; c < MASTER_POINTS.length; c++) {
                if(MASTER_POINTS[0][c][0][1] >= end.point.getY()) {
                    endArr[1] = c;
                    break;
                }
            }

            for(int c = MASTER_POINTS.length - 1; c >= 0; c--) {
                if(MASTER_POINTS[0][c][0][1] <= current.point.getY()) {
                    currentArr[1] = c;
                    break;
                }
            }
        } else {
            for(int c = 0; c < MASTER_POINTS.length; c++) {
                if(MASTER_POINTS[0][c][0][1] >= current.point.getY()) {
                    currentArr[1] = c;
                    break;
                }
            }

            for(int c = MASTER_POINTS.length - 1; c >= 0; c--) {
                if(MASTER_POINTS[0][c][0][1] <= end.point.getY()) {
                    endArr[1] = c;
                    break;
                }
            }
        }

        if(lessZ) {
            for(int d = 0; d < MASTER_POINTS.length; d++) {
                if(MASTER_POINTS[0][0][d][2] >= end.point.getZ()) {
                    endArr[2] = d;
                    break;
                }
            }

            for(int d = MASTER_POINTS.length - 1; d >= 0; d--) {
                if(MASTER_POINTS[0][0][d][2] <= current.point.getZ()) {
                    currentArr[2] = d;
                    break;
                }
            }
        } else {
            for(int d = 0; d < MASTER_POINTS.length; d++) {
                if(MASTER_POINTS[0][0][d][2] >= current.point.getZ()) {
                    currentArr[2] = d;
                    break;
                }
            }

            for(int d = MASTER_POINTS.length - 1; d >= 0; d--) {
                if(MASTER_POINTS[0][0][d][2] <= end.point.getZ()) {
                    endArr[2] = d;
                    break;
                }
            }
        }

        System.gc();

        return new int[][]{currentArr, endArr};
    }

    //Returns the distance between the current point and the point (x,y,z)
    private static double distance(double x, double y, double z, moveData current) {
        return Math.sqrt(Math.pow(current.point.getX() - x, 2) + Math.pow(current.point.getY() - y, 2) + Math.pow(current.point.getZ() - z, 2));
    }

    //Returns all zones that the line between current and y could cross
    public static List<Integer> couldCross(double y, moveData current) {
        List<Integer> output = new ArrayList<Integer>();

        if(y >= current.point.getY()) {
            for(int i = 0; i < outYMin.length; i++) {
                if(outYMin[i] >= current.point.getY()) {
                    output.add(i);
                }
            }
        } else {
            for(int i = 0; i < outYMax.length; i++) {
                if(outYMax[i] <= current.point.getY()) {
                    output.add(i);
                }
            }
        }

        return output;
    }

    //Returns whether or not the point (x,y,z) is in the big KIZ
    public static boolean inBounds(double x, double y, double z) {
        return x <= inXMax && x >= inXMin && y <= inYMax && y >= inYMin && z <= inZMax && z >= inZMin;
    }

    //Calculates whether a given point that isn't the current point touches a given zone
    public static boolean touchesZone(double x, double y, double z, int zoneIndex, moveData current) {
        //t is defined as the time it takes to travel to current. Every variable will take one t to reach current
        if(Math.abs(x - current.point.getX()) > 0.01) {
            double tMin = (outXMin[zoneIndex] - current.point.getX()) / (x - current.point.getX());
            double tMax = (outXMax[zoneIndex] - current.point.getX()) / (x - current.point.getX());
            double yMin = calcY(tMin, y, current), yMax = calcY(tMax, y, current);
            double zMin = calcZ(tMin, z, current), zMax = calcZ(tMax, z, current);
            boolean goodYMin = yMin + AVOIDANCE <= outYMin[zoneIndex] || yMin - AVOIDANCE >= outYMax[zoneIndex];
            boolean goodYMax = yMax + AVOIDANCE <= outYMin[zoneIndex] || yMax - AVOIDANCE >= outYMax[zoneIndex];
            boolean goodZMin = zMin + AVOIDANCE <= outZMin[zoneIndex] || zMin - AVOIDANCE >= outZMax[zoneIndex];
            boolean goodZMax = zMax + AVOIDANCE <= outZMin[zoneIndex] || zMax - AVOIDANCE >= outZMax[zoneIndex];

            if(!(goodYMin || goodZMin)) {
                return true;
            }
            if(!(goodYMax || goodZMax)) {
                return true;
            }
        }
        if(Math.abs(y - current.point.getY()) > 0.01) {
            double tMin = (outYMin[zoneIndex] - current.point.getY()) / (y - current.point.getY());
            double tMax = (outYMax[zoneIndex] - current.point.getY()) / (y - current.point.getY());
            double xMin = calcX(tMin, x, current), xMax = calcX(tMax, x, current);
            double zMin = calcZ(tMin, z, current), zMax = calcZ(tMax, z, current);
            boolean goodXMin = xMin + AVOIDANCE <= outXMin[zoneIndex] || xMin - AVOIDANCE >= outXMax[zoneIndex];
            boolean goodXMax = xMax + AVOIDANCE <= outXMin[zoneIndex] || xMax - AVOIDANCE >= outXMax[zoneIndex];
            boolean goodZMin = zMin + AVOIDANCE <= outYMin[zoneIndex] || zMin - AVOIDANCE >= outYMax[zoneIndex];
            boolean goodZMax = zMax + AVOIDANCE <= outYMin[zoneIndex] || zMax - AVOIDANCE >= outYMax[zoneIndex];

            if(!(goodXMin || goodZMin)) {
                return true;
            }
            if(!(goodXMax || goodZMax)) {
                return true;
            }
        }
        if(Math.abs(z - current.point.getZ()) > 0.01) {
            double tMin = (outZMin[zoneIndex] - current.point.getZ()) / (z - current.point.getZ());
            double tMax = (outZMax[zoneIndex] - current.point.getZ()) / (z - current.point.getZ());
            double xMin = calcX(tMin, x, current), xMax = calcX(tMax, x, current);
            double yMin = calcY(tMin, z, current), yMax = calcY(tMax, y, current);
            boolean goodXMin = xMin + AVOIDANCE <= outXMin[zoneIndex] || xMin - AVOIDANCE >= outXMax[zoneIndex];
            boolean goodXMax = xMax + AVOIDANCE <= outXMin[zoneIndex] || xMax - AVOIDANCE >= outXMax[zoneIndex];
            boolean goodYMin = yMin + AVOIDANCE <= outYMin[zoneIndex] || yMin - AVOIDANCE >= outYMax[zoneIndex];
            boolean goodYMax = yMax + AVOIDANCE <= outYMin[zoneIndex] || yMax - AVOIDANCE >= outYMax[zoneIndex];

            if(!(goodXMin || goodYMin)) {
                return true;
            }
            if(!(goodXMax || goodYMax)) {
                return true;
            }
        }

        return false;
    }

    private static double calcX(double t, double x, moveData current) {
        return current.point.getX() + t * (x - current.point.getX());
    }
    private static double calcY(double t, double y, moveData current) {
        return current.point.getY() + t * (y - current.point.getY());
    }
    private static double calcZ(double t, double z, moveData current) {
        return current.point.getZ() + t * (z - current.point.getZ());
    }

    //Returns a list of possible points given the bounds and steps for calculation
    //MASTER_POINTS obseletes this
    public static List<double[]> possiblePoints(double xMin, double xMax, double yMin, double yMax, double zMin, double zMax, int steps) {
        List<double[]> output = new ArrayList<double[]>();

        for(double x = xMin; x <= xMax; x += (xMax - xMin) / steps) {
            for(double y = yMin; y <= yMax; y += (yMax - yMin) / steps) {
                for(double z = zMin; z <= zMax; z += (zMax - zMin) / steps) {
                    output.add(new double[]{x, y, z});
                }
            }
        }

        return output;
    }
}
