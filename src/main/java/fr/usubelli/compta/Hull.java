package fr.usubelli.compta;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Hull {
    private Mat src;
    //private Mat srcGray = new Mat();
    private JFrame frame;
    private JLabel imgSrcLabel;
    private JLabel imgContoursLabel;
    private static final int MAX_THRESHOLD = 255;
    private int MAX_BLUR = 5;
    private int threshold = 57;
    private int blur = 2;
    private Random rng = new Random(12345);

    public Hull() {
    }

    public void open() {
        //String filename = "D:\\workspace\\compta\\src\\main\\resources\\perspective-deskew-left.jpg";
        String filename = "D:\\workspace\\compta\\src\\main\\resources\\CFXFDZJYYS.jpg";
        src = Imgcodecs.imread(filename);
        if (src.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }

        // Create and set up the window.
        frame = new JFrame("Convex Hull demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set up the content pane.
        Image img = HighGui.toBufferedImage(src);
        addComponentsToPane(frame.getContentPane(), img);
        // Use the content pane's default BorderLayout. No need for
        // setLayout(new BorderLayout());
        // Display the window.
        frame.pack();
        frame.setVisible(true);
        imgContoursLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(transform(src, threshold, blur))));
        frame.repaint();

    }

    private void addComponentsToPane(Container pane, Image img) {
        if (!(pane.getLayout() instanceof BorderLayout)) {
            pane.add(new JLabel("Container doesn't use BorderLayout!"));
            return;
        }
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
        sliderPanel.add(new JLabel("Blur : "));
        sliderPanel.add(blurSlider());
        sliderPanel.add(new JLabel("Canny threshold: "));
        sliderPanel.add(tresholdSlider());
        pane.add(sliderPanel, BorderLayout.PAGE_START);
        JPanel imgPanel = new JPanel();
        imgSrcLabel = new JLabel(new ImageIcon(img));
        imgPanel.add(imgSrcLabel);
        Mat srcGray = new Mat();
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        Mat blackImg = Mat.zeros(srcGray.size(), CvType.CV_8U);
        imgContoursLabel = new JLabel(new ImageIcon(HighGui.toBufferedImage(blackImg)));
        imgPanel.add(imgContoursLabel);
        pane.add(imgPanel, BorderLayout.CENTER);
    }

    private JSlider blurSlider() {
        final ChangeListener changeListener = e -> {
            JSlider source = (JSlider) e.getSource();
            blur = source.getValue();
            imgContoursLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(transform(src, threshold, blur))));
            frame.repaint();
        };
        JSlider slider = new JSlider(1, MAX_BLUR, blur);
        slider.setMajorTickSpacing(2);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(changeListener);
        return slider;
    }

    private JSlider tresholdSlider() {
        final ChangeListener changeListener = e -> {
            JSlider source = (JSlider) e.getSource();
            threshold = source.getValue();
            imgContoursLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(transform(src, threshold, blur))));
            frame.repaint();
        };
        JSlider slider = new JSlider(0, MAX_THRESHOLD, threshold);
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(changeListener);
        return slider;
    }

    public Mat transform(Mat src, int threshold, int blur) {
        Mat srcGray = new Mat();
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        Mat drawing = Mat.zeros(srcGray.size(), CvType.CV_8UC3);
        srcGray.copyTo(drawing);
        Mat cannyOutput = new Mat();
        Imgproc.blur(drawing, drawing, new Size(blur, blur));
        Imgproc.Canny(drawing, cannyOutput, threshold, threshold * 2);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        List<MatOfPoint> hullList = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            MatOfInt hull = new MatOfInt();
            Imgproc.convexHull(contour, hull);
            Point[] contourArray = contour.toArray();
            Point[] hullPoints = new Point[hull.rows()];
            List<Integer> hullContourIdxList = hull.toList();
            for (int i = 0; i < hullContourIdxList.size(); i++) {
                hullPoints[i] = contourArray[hullContourIdxList.get(i)];
            }
            hullList.add(new MatOfPoint(hullPoints));
        }
        RotatedRect rotatedRect = null;
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(0, rng.nextInt(256),0);
            double peri = Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true);
            final MatOfPoint2f approxCurve = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approxCurve,0.02 * peri, true);


            if (peri > 1000 && approxCurve.toArray().length >= 4) {

            System.out.println(i + " " + approxCurve.toArray().length);
                //System.out.println(peri);
                //Imgproc.drawContours(drawing, contours, i, color);
                Scalar green = new Scalar(81, 190, 0);
                rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(approxCurve.toArray()));
                //drawRotatedRect(drawing, rotatedRect, green, 4);
                break;
            }
        }

        final Point[] pt = new Point[4];
        rotatedRect.points(pt);
        Mat destImage = new Mat();
        src.copyTo(destImage);
        Mat msrc = new MatOfPoint2f(new Point(pt[1].x, pt[1].y), new Point(pt[2].x, pt[2].y), new Point(pt[3].x, pt[3].y), new Point(pt[0].x, pt[0].y));
        Mat mdst = new MatOfPoint2f(new Point(0, 0), new Point(destImage.width() - 1, 0), new Point(destImage.width() - 1, destImage.height() - 1), new Point(0, destImage.height() - 1));
        Mat transform = Imgproc.getPerspectiveTransform(msrc, mdst);
        Imgproc.warpPerspective(src, destImage, transform, destImage.size());
        Photo.fastNlMeansDenoisingColored(destImage, destImage);
        Imgproc.threshold(destImage, destImage, 100, 255, Imgproc.THRESH_BINARY);
        Imgproc.cvtColor(destImage, destImage, Imgproc.COLOR_RGB2GRAY);

        return destImage;
    }
    public static void drawRotatedRect(Mat image, RotatedRect rotatedRect, Scalar color, int thickness) {
        Point[] vertices = new Point[4];
        rotatedRect.points(vertices);
        MatOfPoint points = new MatOfPoint(vertices);
        Imgproc.drawContours(image, Arrays.asList(points), -1, color, thickness);
    }
    public static void main(String[] args) {
        // Load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Hull().open();
            }
        });
    }
}

