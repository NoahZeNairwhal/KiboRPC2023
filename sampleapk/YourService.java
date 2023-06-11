package jp.jaxa.iss.kibo.rpc.sampleapk;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
import java.util.List;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    public static KiboRpcApi myApi;
    @Override
    protected void runPlan1() {
        myApi = api;
        myApi.startMission();

        while(myApi.getTimeRemaining().get(1) > 60000) {
            List<Integer> active = myApi.getActiveTargets();

            for(Integer i: active) {
                System.out.println("I'm going to Target " + i);
                CraigMoveTo(i);

                myApi.laserControl(true);
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    System.out.println("Error with laser sleep");
                }
                myApi.takeTargetSnapshot(i);

                if(myApi.getTimeRemaining().get(1) > 60000) {
                    break;
                }
            }
        }

        myApi.notifyGoingToGoal();
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
        List<moveData> dataPoints = ZoneData.intermediateData(TargetData.updated[targetNum - 1]);
        dataPoints.add(TargetData.updated[targetNum - 1]);

        for(moveData dataPoint: dataPoints) {
            int i = 0;
            boolean succeed = false;

            while(!succeed && i < tries) {
                long currTime = myApi.getTimeRemaining().get(1);
                succeed = myApi.moveTo(dataPoint.point, dataPoint.quaternion, dataPoint.print).hasSucceeded();

                while(currTime - myApi.getTimeRemaining().get(1) < 2000) {}
                i++;
            }
        }
    }

    public static void CraigMoveTo(moveData endData, int tries) {
        List<moveData> dataPoints = ZoneData.intermediateData(endData);
        dataPoints.add(endData);

        for(moveData dataPoint: dataPoints) {
            int i = 0;
            boolean succeed = false;

            while(!succeed && i < tries) {
                long currTime = myApi.getTimeRemaining().get(1);
                succeed = myApi.moveTo(dataPoint.point, dataPoint.quaternion, dataPoint.print).hasSucceeded();

                while(currTime - myApi.getTimeRemaining().get(1) < 2000) {}
                i++;
            }
        }
    }
}
