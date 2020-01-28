package fr.usubelli.compta;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.usubelli.compta.domain.nlp.Evaluation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class FileDatabaseEvaluation {

    private final File evaluationJsonFilePath;

    FileDatabaseEvaluation(String path) throws IOException {
        evaluationJsonFilePath = new File(path);
    }

    List<Evaluation> all() {
        final File[] evaluationFolders = evaluationJsonFilePath.listFiles();
        List<Evaluation> evaluations = new ArrayList<>();
        for (File evaluationFolder : evaluationFolders) {
            try {
                evaluations.add(new ObjectMapper().readValue(new File(evaluationFolder, "data.json"), Evaluation.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return evaluations;
    }

    void save(BufferedImage image, Evaluation evaluation) {
        final File folder = new File(evaluationJsonFilePath, UUID.randomUUID().toString());
        folder.mkdirs();
        try {
            ImageIO.write(image, "jpg", new File(folder, "image.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            new ObjectMapper().writeValue(new File(folder, "data.json"), evaluation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
