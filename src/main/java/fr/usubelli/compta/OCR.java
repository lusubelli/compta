package fr.usubelli.compta;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;

import static org.bytedeco.leptonica.global.lept.pixDestroy;
import static org.bytedeco.leptonica.global.lept.pixRead;

class OCR {

    private final TessBaseAPI tessBaseAPI;

    OCR(String tessDataPath, String language) {
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        if (tessBaseAPI.Init(tessDataPath, language) != 0) {
            throw new IllegalStateException("Could not initialize tesseract.");
        }
        this.tessBaseAPI = tessBaseAPI;
    }

    String ocr(String s) {
        PIX image = pixRead(s);
        tessBaseAPI.SetImage(image);
        BytePointer outText = tessBaseAPI.GetUTF8Text();
        tessBaseAPI.End();
        outText.deallocate();
        pixDestroy(image);
        return outText.getString();
    }

}
