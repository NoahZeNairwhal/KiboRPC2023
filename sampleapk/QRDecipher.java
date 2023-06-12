package jp.jaxa.iss.kibo.rpc.sampleapk;

import org.opencv.objdetect.QRCodeDetector;
import org.opencv.core.Mat;

public class QRDecipher {
    //The actual detector
    public static QRCodeDetector detector = new QRCodeDetector();
    //The mat of the navcam, looking at the qrCode
    public static Mat image;
    //The plain string returned by the qr
    public static String qrString;
    //The qrString's mission report equivalent
    public static String reportString = "NO_PROBLEM"; //If something fails then it has a 1/6 chance of being right

    //Use if looking at qr code and you want to decipher it
    public static void decipher() {
        //Stores what navCam sees in the Mat image (navCam needs to be seeing the qrCode)
        image = YourService.myApi.getMatNavCam();
        //Get the plain decipher of the qr code
        qrString = detector.detectAndDecode(image);

        //Match it up with the correct report message
        if(qrString.equals("JEM")) {
            reportString = "STAY_AT_JEM";
        } else if(qrString.equals("COLUMBUS")) {
            reportString = "GO_TO_COLUMBUS";
        } else if(qrString.equals("RACK1")) {
            reportString = "CHECK_RACK_1";
        } else if(qrString.equals("ASTROBEE")) {
            reportString = "I_AM_HERE";
        } else if(qrString.equals("INTBALL")) {
            reportString = "LOOKING_FORWARD_TO_SEE_YOU";
        } else {
            reportString = "NO_PROBLEM";
        }
    }

}
