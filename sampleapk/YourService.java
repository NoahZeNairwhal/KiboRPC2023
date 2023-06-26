package jp.jaxa.iss.kibo.rpc.sampleapk;

import org.apache.commons.logging.*;

import ff_msgs.Zone;
import gov.nasa.arc.astrobee.types.Point;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import java.lang.annotation.Target;
import java.util.List;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    //Use logger.info(String) or other logger methods if you want to log things in the log file
    public static final Log logger = LogFactory.getLog(YourService.class);
    //So that I can access the api from other classes in this package
    public static KiboRpcApi myApi;
    public static boolean moveToGoal = false;
    public static boolean bypass = false;
    public static boolean scanned = false;
    public static moveData current;
    public static final double BUFFER_TIME = 0;
    public static int prevTarg = 7;

    @Override
    protected void runPlan1() {
        myApi = api;
        myApi.startMission();

        for(int i = 0; i < 5 && !scanned; i++) {
            CraigMoveTo(7);
            QRDecipher.decipher();
        }

        prevTarg = 7;

        while(!moveToGoal) {
            List<Integer> active = myApi.getActiveTargets();
            List<Integer> actual = TargetData.determiner(prevTarg, active);

            for(Integer i: actual) {
                if(TargetData.times[prevTarg - 1][i - 1] + TargetData.times[i - 1][7] + BUFFER_TIME > myApi.getTimeRemaining().get(1)) {
                    moveToGoal = true;
                    break;
                }
                if(TargetData.times[prevTarg - 1][i - 1] > myApi.getTimeRemaining().get(0)) {
                    break;
                }

                logger.info("I should be going to Target #" + i);

                CraigMoveTo(i);

                logger.info("I should be at Target #" + i);

                if(TargetData.updated[i - 1].equals(myApi.getRobotKinematics())) {
                    //Activate laser
                    myApi.laserControl(true);
                    try {
                        Thread.sleep(1000);
                    } catch(InterruptedException e) {
                        logger.error("Error with laser sleep");
                    }
                    //Screenshot target, automatically deactivates laser
                    myApi.takeTargetSnapshot(i);
                }

                prevTarg = i;
            }
        }

        bypass = true;
        //8 is the goal
        logger.info("Goal moveTo start");
        CraigMoveTo(8);
        logger.info("Goal moveTo end");
        myApi.reportMissionCompletion(QRDecipher.reportString);
    }

    @Override
    protected void runPlan2() {
       // write your plan 2 here
    }

    @Override
    protected void runPlan3() {
        // write your plan 3 here
    }

    public static void CraigMoveTo(int targetNum) {
        CraigMoveTo(targetNum, 2);
    }

    /*public static void CraigMoveTo(moveData endData) {
        CraigMoveTo(endData, 3);
    }*/

    public static void CraigMoveTo(int targetNum, int tries) {
        for(int j = 0; j <= 5 && !TargetData.updated[targetNum - 1].equals(myApi.getRobotKinematics()); j++) {
            ZoneData.AVOIDANCE = 0.15 - 0.03 * j;
            //Gets the list of points to go to
            List<moveData> dataPoints = ZoneData.intermediateData(TargetData.updated[targetNum - 1]);
            //Adds the endpoint (otherwise it wouldn't go to the point it's supposed to)
            dataPoints.add(TargetData.updated[targetNum - 1]);

            for (int i = 0; i < dataPoints.size(); i++) {
                int counter = 0;
                boolean succeeded = false;

                while (!succeeded && counter < tries) {
                    succeeded = myApi.moveTo(dataPoints.get(i).point, dataPoints.get(i).quaternion, dataPoints.get(i).print).hasSucceeded();
                    counter++;
                }
            }
        }

        /*if(targetNum <= 6) {
            logger.info("ARDetector start");
            ARDetector.detect();
            logger.info("ARDetector end");
        }*/
    }

    /*public static double getTime(double distance) {
        //I <3 calc
        double delta = 0.01;
        double tot = 0.0;
        double a = 0.02;
        double v = 0.0;
        double p = 0.0;

        while(p < distance) {
            v = Math.min(v + (a * delta), 0.4);
            p += 2 * (v * delta);
            tot += delta;
        }

        return tot * 2;
    }


    //Same as above method but with a specific moveData
    public static void CraigMoveTo(moveData endData, int tries) {
        //Gets the list of points to go to
        List<moveData> dataPoints = ZoneData.intermediateData(endData);
        //Adds the endpoint (otherwise it wouldn't go to the point it's supposed to)
        dataPoints.add(endData);

        for(moveData dataPoint: dataPoints) {
            int i = 0;
            boolean succeed = false;

            //i < tries prevents an infinite loop
            while(!succeed && i < tries) {
                if(myApi.getTimeRemaining().get(1) <= 120000 && !bypass) {
                    moveToGoal = true;
                    return;
                }

                succeed = myApi.moveTo(dataPoint.point, dataPoint.quaternion, dataPoint.print).hasSucceeded();
                i++;
            }
        }
    }*/
}