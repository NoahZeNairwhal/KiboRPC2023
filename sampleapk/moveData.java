package jp.jaxa.iss.kibo.rpc.sampleapk;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import gov.nasa.arc.astrobee.Kinematics;

public class moveData {
    public Point point;
    public Quaternion quaternion;
    public boolean print;
    public static final double P_TOLERANCE = 0.35;
    public static final double A_TOLERANCE = 0.2;

    public moveData() {
        Kinematics currentKinematics = YourService.myApi.getRobotKinematics();
        point = currentKinematics.getPosition();
        quaternion = currentKinematics.getOrientation();
        print = true;
    }

    public moveData(boolean aPrint) {
        Kinematics currentKinematics = YourService.myApi.getRobotKinematics();
        point = currentKinematics.getPosition();
        quaternion = currentKinematics.getOrientation();
        print = aPrint;
    }
    
    public moveData (Point aPoint, Quaternion aQuaternion) {
        point = aPoint;
        quaternion = aQuaternion;
        print = true;
    }

    public moveData (Point aPoint, Quaternion aQuaternion, boolean aPrint) {
        point = aPoint;
        quaternion = aQuaternion;
        print = aPrint;
    }

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
