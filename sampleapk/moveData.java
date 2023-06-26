package jp.jaxa.iss.kibo.rpc.sampleapk;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import gov.nasa.arc.astrobee.Kinematics;

public class moveData {
    //The (x,y,z) Point for Astrobee to move to
    public Point point;
    //The quaternion representation of Astrobee at this point
    public Quaternion quaternion;
    //Whether or not to log the position
    public boolean print;
    //The position tolerance of Astrobee when this moveData is checked against the current Kinematics
    public static final double P_TOLERANCE = 0.2;
    //Same as above but for the angle (radians)
    public static final double A_TOLERANCE = 0.1;
    public double totDistance = 0.0;

    //Creates a moveData representation of the current Kinematics
    public moveData() {
        Kinematics currentKinematics = YourService.myApi.getRobotKinematics();
        point = currentKinematics.getPosition();
        quaternion = currentKinematics.getOrientation();
        print = true;
    }

    //Given whether or not to print, uses current Kinematics
    public moveData(boolean aPrint) {
        Kinematics currentKinematics = YourService.myApi.getRobotKinematics();
        point = currentKinematics.getPosition();
        quaternion = currentKinematics.getOrientation();
        print = aPrint;
    }

    //Given the info for some point other than it's current point
    public moveData (Point aPoint, Quaternion aQuaternion) {
        point = aPoint;
        quaternion = aQuaternion;
        print = true;
    }

    //Same as above but also given whether or not to print
    public moveData (Point aPoint, Quaternion aQuaternion, boolean aPrint) {
        point = aPoint;
        quaternion = aQuaternion;
        print = aPrint;
    }

    //Returns if this moveData is equal to the current Kinematics, taking into account the tolerances
    public boolean equals(Kinematics current) {
        return Math.abs(point.getX() - current.getPosition().getX()) <= P_TOLERANCE
        && Math.abs(point.getY() - current.getPosition().getY()) <= P_TOLERANCE
        && Math.abs(point.getZ() - current.getPosition().getZ()) <= P_TOLERANCE
        && Math.abs(quaternion.getX() - current.getOrientation().getX()) <= A_TOLERANCE
        && Math.abs(quaternion.getY() - current.getOrientation().getY()) <= A_TOLERANCE
        && Math.abs(quaternion.getZ() - current.getOrientation().getZ()) <= A_TOLERANCE
        && Math.abs(quaternion.getW() - current.getOrientation().getW()) <= A_TOLERANCE;
    }
}
