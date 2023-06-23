package jp.jaxa.iss.kibo.rpc.sampleapk;

import org.apache.commons.logging.*;

import ff_msgs.Zone;
import gov.nasa.arc.astrobee.types.Point;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
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

    @Override
    protected void runPlan1() {
        myApi = api;
        myApi.startMission();

        //While more than a minute total time remains
        while(!moveToGoal) {
            moveData current = new moveData();
            List<Integer> active = myApi.getActiveTargets();

            for(int i = 0; i < active.size(); i++) {
                double leastDistance = ZoneData.distance(current.point.getX(), current.point.getY(), current.point.getZ(), TargetData.updated[active.get(i)]);

                for(int j = i + 1; j < active.size(); j++) {
                    double tempDistance = ZoneData.distance(current.point.getX(), current.point.getY(), current.point.getZ(), TargetData.updated[active.get(j)]);

                    if(tempDistance < leastDistance) {
                        int temp = active.get(i);
                        active.set(i, active.get(j));
                        active.set(j, temp);
                    }
                }
            }

            //For each active target
            for(Integer i: active) {
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

                //Checks again in case a lot of targets are active, so that it can break out and go to the goal instead of continuing to snapshot targets
                if(myApi.getTimeRemaining().get(1) <= 120000 && !bypass) {
                    moveToGoal = true;
                    break;
                }
            }
        }

        bypass = true;
        logger.info("QRCode moveTo start");
        //7 is the QR Code
        CraigMoveTo(7);
        logger.info("QRCode moveTo end");
        logger.info("QRCode decipher start");
        QRDecipher.decipher();
        logger.info("QRCode decipher end");
        myApi.notifyGoingToGoal();
        /*logger.info("Down movement start");
        //The Bee runs into a lot of KOZ violations trying to move directly to the goal, so we're going to move it down a bit first
        int num = 0;
        boolean succeed = false;

        while(num < 3 && !succeed) {
            succeed = myApi.moveTo(new Point(11.381944, -8.8, 5), myApi.getRobotKinematics().getOrientation(), true).hasSucceeded();
            num++;
        }

        logger.info("Down movement end");*/
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
        CraigMoveTo(targetNum, 3);
    }

    /*public static void CraigMoveTo(moveData endData) {
        CraigMoveTo(endData, 3);
    }*/

    public static void CraigMoveTo(int targetNum, int tries) {
        for(int j = 0; j <= 10 && !TargetData.updated[targetNum - 1].equals(myApi.getRobotKinematics()); j++) {
            ZoneData.AVOIDANCE = 0.15 - 0.015 * j;
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

                if (myApi.getTimeRemaining().get(1) <= 120000 && !bypass) {
                    moveToGoal = true;
                    return;
                }
            }
        }

        if(targetNum <= 6) {
            logger.info("ARDetector start");
            ARDetector.detect();
            logger.info("ARDetector end");
        }
    }

    /*//Same as above method but with a specific moveData
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
