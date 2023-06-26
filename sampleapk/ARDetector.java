package jp.jaxa.iss.kibo.rpc.sampleapk;

import org.apache.commons.lang.ObjectUtils;
import org.opencv.aruco.Aruco;
import org.opencv.core.Mat;
import java.util.List;
import java.util.ArrayList;

public class ARDetector {
    //The image from the navCam
    public static Mat image;
    //The four corners of each of the detected AR codes
    public static List<Mat> corners;
    //The ids of each of the detected AR codes
    public static Mat ids;

    //The offsets in each direction of the navCam
    public static final double xNav = 0.1177, yNav = -0.0422, zNav = -0.0826;
    //The offsets in each direction of the laser pointer
    public static final double xLaser = 0.1302, yLaser = 0.0572, zLaser = -0.1111;


    //Returns a moveData object of where to move to in order to best hit the target
    public static moveData detect() {
        image = new Mat();
        ids = new Mat();
        corners = new ArrayList<Mat>();

        image = YourService.myApi.getMatNavCam();

        for(int i = 0; i < 4 && image == null; i++) {
            try {
               Thread.sleep(250);
            } catch(InterruptedException e) {
                YourService.logger.info("Error while sleeping during an image reset");
            }

            image = YourService.myApi.getMatNavCam();
        }

        if(image == null) {
            try {
                Thread.sleep(500);
            } catch(InterruptedException e) {
                YourService.logger.info("Error while sleeping during a marker detection reset");
            }

            YourService.logger.info("ARDetctor.detect() could not execute since image was null");
            return new moveData();
        }

        //YourService.myApi.saveMatImage(image, "A Target Image");

        Aruco.detectMarkers(image, Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250), corners, ids);

        for(int i = 0; i < 4 && ids == null && corners != null && corners.size() == 0; i++) {
            Aruco.detectMarkers(image, Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250), corners, ids);
        }

        if(ids == null) {
            YourService.logger.info("ARDetctor.detect() could not execute since ids was null");

            if(corners == null || corners.size() == 0) {
                YourService.logger.info("ARDetctor.detect() could not execute since corners had a size of 0 or it was null");
            }

            return new moveData();
        } else if(corners == null || corners.size() == 0) {
            YourService.logger.info("ARDetctor.detect() could not execute since corners had a size of 0 or it was null");
            return new moveData();
        }

        List<double[]> centers = new ArrayList<double[]>();

        for (Mat marker : corners) {
            if (marker == null) {
                YourService.logger.info("One of the corners seems to have been null");
                continue;
            } else {
                YourService.logger.info("A marker: " + marker.dump());
                double xAvg = 0.0;
                double zAvg = 0.0;

                for(int c = 0; c < marker.cols(); c++) {
                    xAvg += marker.get(0, c)[0];
                    zAvg += marker.get(0, c)[1];
                }

                xAvg /= 4.0;
                zAvg /= 4.0;

                centers.add(new double[]{xAvg, zAvg});

                YourService.logger.info("X center: " + centers.get(centers.size() - 1)[0] + ", Z center: " + centers.get(centers.size() - 1)[1]);
            }
        }

        for(int r = 0; r < ids.rows(); r++) {
            for(int c = 0; c < ids.cols(); c++) {
                for(int i = 0; i < ids.get(r, c).length; i++) {
                    YourService.logger.info("Ids--- Row: " + r + ", Column: " + c + ", Index: " + i + ", Value: " + ids.get(r, c)[i]);
                }
            }
        }

        double estimatedTargetX = 0.0;
        double estimatedTargetZ = 0.0;

        for(int r = 0; r < ids.rows(); r++) {
            double[] arr = centers.get(r);
            double pixelPerMetre = (corners.get(r).get(0, 0)[0] - corners.get(r).get(0, 2)[0] / (5 / 100.0) + corners.get(r).get(0, 0)[1] - corners.get(r).get(0, 2)[1] / (5 / 100.0)) / 2.0;

            switch((int) ids.get(r, 0)[0] % 4) {
                case 1:
                    YourService.logger.info("ID: " + ids.get(r, 0)[0] + ", Estimated X: " + (arr[0] - pixelPerMetre * (10 / 100.0)) + ", Estimated Z: " + (arr[1] - pixelPerMetre * (3.75 / 100.0)));
                    break;
                case 2:
                    YourService.logger.info("ID: " + ids.get(r, 0)[0] + ", Estimated X: " + (arr[0] + pixelPerMetre * (10 / 100.0)) + ", Estimated Z: " + (arr[1] - pixelPerMetre * (3.75 / 100.0)));
                    break;
                case 3:
                    YourService.logger.info("ID: " + ids.get(r, 0)[0] + ", Estimated X: " + (arr[0] + pixelPerMetre * (10 / 100.0)) + ", Estimated Z: " + (arr[1] + pixelPerMetre * (3.75 / 100.0)));
                    break;
                case 0:
                    YourService.logger.info("ID: " + ids.get(r, 0)[0] + ", Estimated X: " + (arr[0] - pixelPerMetre * (10 / 100.0)) + ", Estimated Z: " + (arr[1] + pixelPerMetre * (3.75 / 100.0)));
                    break;
            }
        }

        image.release();
        ids.release();
        for(Mat a: corners) {
            a.release();
        }
        image = null;
        ids = null;
        corners = null;

        System.gc();
        System.runFinalization();

        return new moveData();
    }
}
/*
public static void detectMarkers​(Mat image, Dictionary dictionary, java.util.List<Mat> corners, Mat ids)
Basic marker detection
Parameters:
image - input image
dictionary - indicates the type of markers that will be searched
corners - vector of detected marker corners. For each marker, its four corners are provided,
(e.g std::vector<std::vector<cv::Point2f> > ). For N detected markers, the dimensions of this array is Nx4.
The order of the corners is clockwise.
ids - vector of identifiers of the detected markers. The identifier is of type int (e.g. std::vector<int>).
For N detected markers, the size of ids is also N. The identifiers have the same order than the markers in the imgPoints array.
correct codification. Useful for debugging purposes. Performs marker detection in the input image.
Only markers included in the specific dictionary are searched. For each detected marker, it returns the 2D position of its corner
in the image and its corresponding identifier. Note that this function does not perform pose estimation.
Note: The function does not correct lens distortion or takes it into account. It's recommended to undistort input image
with corresponging camera model, if camera parameters are known SEE: undistort, estimatePoseSingleMarkers, estimatePoseBoard
 */