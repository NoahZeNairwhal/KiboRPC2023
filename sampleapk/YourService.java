package jp.jaxa.iss.kibo.rpc.sampleapk;

import org.apache.commons.logging.*;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
import java.util.List;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    //Use logger.info(String) or other logger methods if you want to log things in the log file
    private static final Log logger = LogFactory.getLog(YourService.class);
    //So that I can access the api from other classes in this package
    public static KiboRpcApi myApi;
    @Override
    protected void runPlan1() {
        myApi = api;
        myApi.startMission();

        //While more than a minute total time remains
        while(myApi.getTimeRemaining().get(1) > 75000) {
            List<Integer> active = myApi.getActiveTargets();

            //For each active target
            for(Integer i: active) {
                CraigMoveTo(i);
                logger.info("I should be at Target #" + i);

                //Activate laser
                myApi.laserControl(true);
                try {
                    Thread.sleep(500);
                } catch(InterruptedException e) {
                    logger.error("Error with laser sleep");
                }
                //Screenshot target, automatically deactivates laser
                myApi.takeTargetSnapshot(i);

                //Checks again in case a lot of targets are active, so that it can break out and go to the goal instead of continuing to snapshot targets
                if(myApi.getTimeRemaining().get(1) > 75000) {
                    break;
                }
            }
        }

        myApi.notifyGoingToGoal();
        //8 is the goal
        CraigMoveTo(8);
        myApi.reportMissionCompletion("I_AM_HERE");
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

    public static void CraigMoveTo(moveData endData) {
        CraigMoveTo(endData, 3);
    }

    public static void CraigMoveTo(int targetNum, int tries) {
        //Gets the list of points to go to
        List<moveData> dataPoints = ZoneData.intermediateData(TargetData.updated[targetNum - 1]);
        //Adds the endpoint (otherwise it wouldn't go to the point it's supposed to)
        dataPoints.add(TargetData.updated[targetNum - 1]);

        for(moveData dataPoint: dataPoints) {
            int i = 0;
            boolean succeed = false;

            //i < tries prevents an infinite loop
            while(!succeed && i < tries) {
                succeed = myApi.moveTo(dataPoint.point, dataPoint.quaternion, dataPoint.print).hasSucceeded();
                i++;
            }
        }
    }

    //Same as above method but with a specific moveData
    public static void CraigMoveTo(moveData endData, int tries) {
        List<moveData> dataPoints = ZoneData.intermediateData(endData);
        dataPoints.add(endData);

        for(moveData dataPoint: dataPoints) {
            int i = 0;
            boolean succeed = false;

            while(!succeed && i < tries) {
                succeed = myApi.moveTo(dataPoint.point, dataPoint.quaternion, dataPoint.print).hasSucceeded();
                i++;
            }
        }
    }
}
