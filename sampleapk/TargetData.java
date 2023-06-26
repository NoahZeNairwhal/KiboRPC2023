package jp.jaxa.iss.kibo.rpc.sampleapk;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import java.util.*;

public class TargetData {
    //The intial position of all the targets, + the qr code, + the goal
    public static moveData[] intial = initialize();
    //The updated position of all the targets, + the qr code, + the goal (updateTarget changes it)
    public static moveData[] updated = initialize();
    public static double[][] times = times();

    //All the moveData representations of the intial values of the targets and whatnot
    public static moveData[] initialize() {
        return new moveData[]{
                new moveData(new Point(11.2056, -9.92284, 5.4768), new Quaternion(0f, 0f, -0.707f, 0.707f)),
                /*new moveData(new Point(10.612, -9.0709, 4.48), new Quaternion(0.5f, 0.5f, -0.5f, 0.5f)),*/
                new moveData(new Point(10.513384 + 0.107, -9.085172 - 0.0572, 4.5), new Quaternion(0f, 0.707f, 0f, 0.707f)),
                new moveData(new Point(10.71 + 0.003, -7.7 - 0.068, 4.48), new Quaternion(0f, 0.707f, 0f, 0.707f)),
                new moveData(new Point(10.51, -6.7185 + 0.104, 5.1804 + 0.027), new Quaternion(0f, 0f, -1f, 0f)),
                new moveData(new Point(11.114 - 0.068, -7.9756 + 0.059, 5.3393), new Quaternion(-0.5f, -0.5f, -0.5f, 0.5f)),
                new moveData(new Point(11.355, -9.0369, 4.9378), new Quaternion(0f, 0f, 0f, 1f)),
                new moveData(new Point(11.381944, -8.566172, 4.5), new Quaternion(0f, 0.707f, 0f, -0.707f)), //Actual z of qr is 3.76203, but that's out of bounds
                new moveData(new Point(11.143, -6.7607, 4.9654), new Quaternion(0f, 0f, -0.707f, 0.707f))};
    }

    public static double[][] times() {
        return new double[][]{
                new double[]{0.0, 50272.0, 66080.0, 40600.0, 31048.0, 25244.0, 31416.0, 67952.0},
                new double[]{50272.0, 0.0, 39408.0, 57856.0, 67896.0, 29840.0, 40536.0, 61232.0},
                new double[]{66080.0, 39408.0, 0.0, 37488.0, 38376.0, 51464.0, 56520, 27264.0},
                new double[]{40600.0, 57856.0, 37488.0, 0.0, 31456.0, 0.0, 68296.0, 18480.0},
                new double[]{31048.0, 67896.0, 38376.0, 31456.0, 0.0, 42216.0, 35024.0, 32592.0},
                new double[]{25244.0, 29840.0, 51464.0, 64520.0, 42216.0, 0.0, 37432.0, 33040.0},
                new double[]{31416.0, 40536.0, 56520, 68296.0, 35024.0, 37432.0, 0.0, 57096.0},
                new double[]{67952.0, 61232.0, 27264.0, 18480.0, 32592.0, 33040.0, 57096.0, 0.0}};
    }

    //Updates target data using current position as well as the target number (1-8)
    public static void updateTarget(int targetNum) {
        updated[targetNum - 1] = new moveData();
    }

    public static List<Integer> determiner(int current, List<Integer> activeTargets) {
        List<Integer> output = new ArrayList<Integer>();
        List<Double> comparer = new ArrayList<Double>();
        List<List<Integer>> orders = new ArrayList<List<Integer>>();

        if(activeTargets.size() == 1) {
            if(!YourService.scanned) {
                if(times[current][6] + times[6][activeTargets.get(0)] > YourService.myApi.getTimeRemaining().get(0)) {
                    output.add(7);
                } else {
                    output.add(7);
                    output.add(activeTargets.get(0));
                }
            } else {
                output.add(activeTargets.get(0));
            }
        } else if(activeTargets.size() == 2) {
            if(!YourService.scanned) {
                double aqb = times[current][activeTargets.get(0)] + times[activeTargets.get(0)][6] + times[6][activeTargets.get(1)];
                double bqa = times[current][activeTargets.get(1)] + times[activeTargets.get(1)][6] + times[6][activeTargets.get(0)];
                double qab = times[current][6] + times[6][activeTargets.get(0)] + times[activeTargets.get(0)][activeTargets.get(1)];
                double qba = times[current][6] + times[6][activeTargets.get(1)] + times[activeTargets.get(1)][activeTargets.get(0)];

                if(Math.min(aqb, Math.min(bqa, Math.min(qab, qba))) > YourService.myApi.getTimeRemaining().get(0)) {
                    double aq = aqb - times[6][activeTargets.get(1)];
                    double bq = bqa - times[6][activeTargets.get(0)];
                    double qa = qab - times[activeTargets.get(0)][activeTargets.get(1)];
                    double qb = qba - times[activeTargets.get(1)][activeTargets.get(0)];

                    if(Math.min(aq, Math.min(bq, Math.min(qa, qb))) > YourService.myApi.getTimeRemaining().get(0)) {
                        output.add(7);
                    } else if(aq <= bq && aq <= qa && aq <= qb) {
                        output.add(activeTargets.get(0));
                        output.add(7);
                    } else if(bq <= aq && bq <= qa && bq <= qb) {
                        output.add(activeTargets.get(1));
                        output.add(7);
                    } else if(qa <= aq && qa <= bq && qa <= qb) {
                        output.add(7);
                        output.add(activeTargets.get(0));
                    } else {
                        output.add(7);
                        output.add(activeTargets.get(1));
                    }
                } else if(aqb <= bqa && aqb <= qab && aqb <= qba) {
                    output.add(activeTargets.get(0));
                    output.add(7);
                    output.add(activeTargets.get(1));
                } else if(bqa <= aqb && bqa <= qab && bqa <= qba) {
                    output.add(activeTargets.get(1));
                    output.add(7);
                    output.add(activeTargets.get(1));
                } else if(qab <= aqb && qab <= bqa && qab <= qba) {
                    output.add(7);
                    output.add(activeTargets.get(0));
                    output.add(activeTargets.get(1));
                } else {
                    output.add(7);
                    output.add(activeTargets.get(1));
                    output.add(activeTargets.get(0));
                }
            } else {
                double ab = times[current][activeTargets.get(0)] + times[activeTargets.get(0)][activeTargets.get(1)];
                double ba = times[current][activeTargets.get(1)] + times[activeTargets.get(1)][activeTargets.get(0)];

                if(Math.min(ab, ba) > YourService.myApi.getTimeRemaining().get(0)) {
                    if(times[current][activeTargets.get(0)] <= times[current][activeTargets.get(1)]) {
                        output.add(activeTargets.get(0));
                    } else {
                        output.add(activeTargets.get(1));
                    }
                } else if(ab <= ba) {
                    output.add(activeTargets.get(0));
                    output.add(activeTargets.get(1));
                } else {
                    output.add(activeTargets.get(1));
                    output.add(activeTargets.get(0));
                }
            }

        } else if(activeTargets.size() == 3) {
            //if(!YourService.scanned) {

            //} else {
                double abc = times[current][activeTargets.get(0)] + times[activeTargets.get(0)][activeTargets.get(1)] + times[activeTargets.get(1)][activeTargets.get(2)];
                double acb = times[current][activeTargets.get(0)] + times[activeTargets.get(0)][activeTargets.get(2)] + times[activeTargets.get(2)][activeTargets.get(1)];
                double bac = times[current][activeTargets.get(1)] + times[activeTargets.get(1)][activeTargets.get(0)] + times[activeTargets.get(0)][activeTargets.get(2)];
                double bca = times[current][activeTargets.get(1)] + times[activeTargets.get(1)][activeTargets.get(2)] + times[activeTargets.get(2)][activeTargets.get(0)];
                double cab = times[current][activeTargets.get(2)] + times[activeTargets.get(2)][activeTargets.get(0)] + times[activeTargets.get(0)][activeTargets.get(1)];
                double cba = times[current][activeTargets.get(2)] + times[activeTargets.get(2)][activeTargets.get(1)] + times[activeTargets.get(1)][activeTargets.get(0)];
                double min = Math.min(abc, Math.min(acb, Math.min(bac, Math.min(bca, Math.min(cab, cba)))));

                if(min > YourService.myApi.getTimeRemaining().get(0)) {
                    double ab = abc - times[activeTargets.get(1)][activeTargets.get(2)];
                    double ac = acb - times[activeTargets.get(2)][activeTargets.get(1)];
                    double ba = bac - times[activeTargets.get(0)][activeTargets.get(2)];
                    double bc = bca - times[activeTargets.get(2)][activeTargets.get(0)];
                    double ca = cab - times[activeTargets.get(0)][activeTargets.get(1)];
                    double cb = cba - times[activeTargets.get(1)][activeTargets.get(0)];
                    double minnie = Math.min(ab, Math.min(ac, Math.min(ba, Math.min(bc, Math.min(ca, cb)))));

                    if(minnie > YourService.myApi.getTimeRemaining().get(0)) {
                        double a = times[current][activeTargets.get(0)];
                        double b = times[current][activeTargets.get(1)];
                        double c = times[current][activeTargets.get(2)];

                        if(a <= b && a <= c) {
                            output.add(activeTargets.get(0));
                        } else if(b <= a && b <= c) {
                            output.add(activeTargets.get(1));
                        } else {
                            output.add(activeTargets.get(2));
                        }
                    } else if(minnie == ab) {
                        output.add(activeTargets.get(0));
                        output.add(activeTargets.get(1));
                    } else if(minnie == ac) {
                        output.add(activeTargets.get(0));
                        output.add(activeTargets.get(2));
                    } else if(minnie == ba) {
                        output.add(activeTargets.get(1));
                        output.add(activeTargets.get(0));
                    } else if(minnie == bc) {
                        output.add(activeTargets.get(1));
                        output.add(activeTargets.get(2));
                    } else if(minnie == ca) {
                        output.add(activeTargets.get(2));
                        output.add(activeTargets.get(0));
                    } else {
                        output.add(activeTargets.get(2));
                        output.add(activeTargets.get(1));
                    }
                } else if(min == abc) {
                    output.add(activeTargets.get(0));
                    output.add(activeTargets.get(1));
                    output.add(activeTargets.get(2));
                } else if(min == acb) {
                    output.add(activeTargets.get(0));
                    output.add(activeTargets.get(2));
                    output.add(activeTargets.get(1));
                } else if(min == bac) {
                    output.add(activeTargets.get(1));
                    output.add(activeTargets.get(0));
                    output.add(activeTargets.get(2));
                } else if(min == bca) {
                    output.add(activeTargets.get(1));
                    output.add(activeTargets.get(2));
                    output.add(activeTargets.get(0));
                } else if(min == cab) {
                    output.add(activeTargets.get(2));
                    output.add(activeTargets.get(0));
                    output.add(activeTargets.get(1));
                } else {
                    output.add(activeTargets.get(2));
                    output.add(activeTargets.get(1));
                    output.add(activeTargets.get(0));
                }
            }
        //}

        return output;
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