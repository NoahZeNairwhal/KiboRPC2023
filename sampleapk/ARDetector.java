package jp.jaxa.iss.kibo.rpc.sampleapk;

import org.opencv.aruco.Aruco;
import org.opencv.core.Mat;
import java.util.List;
import java.util.ArrayList;

public class ARDetector {
    //The detector for the AR codes
    public static Aruco detector = new Aruco();
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
        image = YourService.myApi.getMatNavCam();
        detector.detectMarkers(image, detector.getPredefinedDictionary(Aruco.DICT_5X5_250), corners, ids);

        for(Mat corner: corners) {
            YourService.logger.info("Cols: " + corner.cols());
            YourService.logger.info("Rows: " + corner.rows());
            YourService.logger.info("Depth: " + corner.depth());
        }
        List<Double> centers = new ArrayList<Double>();

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