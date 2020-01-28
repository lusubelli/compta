package fr.usubelli.compta;

import fr.usubelli.compta.domain.nlp.Evaluation;
import fr.usubelli.compta.domain.nlp.IntentEvaluation;
import fr.usubelli.compta.domain.nlp.NameEvaluation;
import fr.usubelli.compta.domain.nlp.QualifiedSentence;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.openimaj.image.DisplayUtilities;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class BasicExample {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws IOException {

        final String directory = "C:\\dev\\workspace\\compta\\src\\main\\resources\\";
        final String filename = directory + "CFXFDZJYYS.jpg";
        final Mat image = Imgcodecs.imread(filename);
        final Mat imageReworked = new Hull().transform(image, 57, 2);
        DisplayUtilities.display((BufferedImage) HighGui.toBufferedImage(image));
        DisplayUtilities.display((BufferedImage) HighGui.toBufferedImage(imageReworked));

        String tessDataPath = directory + "tessdata";
        String language = "fra";


        final String s = directory + "CFXFDZJYYS_rework.jpg";
        ImageIO.write((BufferedImage) HighGui.toBufferedImage(imageReworked), "jpg", new FileOutputStream(s));
        String text = new OCR(tessDataPath, language).ocr(s);
        System.out.println("OCR output:\n" + text);


        final List<QualifiedSentence> sentences = new FileDatabaseQualifiedSentences(directory + "nlp\\qualified.json").all();
        final FileDatabaseEvaluation fileDatabaseEvaluation = new FileDatabaseEvaluation(directory + "nlp\\evaluations");
        final Evaluation evaluation = new NlpEvaluation(sentences, Locale.FRENCH).evaluate(text);
        fileDatabaseEvaluation.save((BufferedImage) HighGui.toBufferedImage(imageReworked), evaluation);


        final List<IntentEvaluation> intentEvaluations = evaluation.getIntentEvaluations();
        IntentEvaluation bestIntentEvaluation = null;
        for (IntentEvaluation intentEvaluation : intentEvaluations) {
            if (bestIntentEvaluation == null) {
                bestIntentEvaluation = intentEvaluation;
            } else {
                if (intentEvaluation.getProbability() > bestIntentEvaluation.getProbability()) {
                    bestIntentEvaluation = intentEvaluation;
                }
            }
        }

        NameEvaluation bestTTCNameEvaluation = null;
        NameEvaluation bestTVANameEvaluation = null;
        NameEvaluation bestDateNameEvaluation = null;
        for (NameEvaluation nameEvaluation : evaluation.getNameEvaluations()) {
            if (nameEvaluation.getType().equals("TTC")) {
                if (bestTTCNameEvaluation == null) {
                    bestTTCNameEvaluation = nameEvaluation;
                } else {
                    if (nameEvaluation.getProbability() > bestTTCNameEvaluation.getProbability()) {
                        bestTTCNameEvaluation = nameEvaluation;
                    }
                }
            }
            if (nameEvaluation.getType().equals("TVA")) {
                if (bestTVANameEvaluation == null) {
                    bestTVANameEvaluation = nameEvaluation;
                } else {
                    if (nameEvaluation.getProbability() > bestTVANameEvaluation.getProbability()) {
                        bestTVANameEvaluation = nameEvaluation;
                    }
                }
            }
            if (nameEvaluation.getType().equals("date")) {
                if (bestDateNameEvaluation == null) {
                    bestDateNameEvaluation = nameEvaluation;
                } else {
                    if (nameEvaluation.getProbability() > bestDateNameEvaluation.getProbability()) {
                        bestDateNameEvaluation = nameEvaluation;
                    }
                }
            }
        }


        System.out.println("Type:" + bestIntentEvaluation.getOutcome());
        System.out.println("TTC:" + text.substring(bestTTCNameEvaluation.getStart(), bestTTCNameEvaluation.getEnd()));
        System.out.println("TVA:" + text.substring(bestTVANameEvaluation.getStart(), bestTVANameEvaluation.getEnd()));
        System.out.println("Date:" + text.substring(bestDateNameEvaluation.getStart(), bestDateNameEvaluation.getEnd()));

    }

}
