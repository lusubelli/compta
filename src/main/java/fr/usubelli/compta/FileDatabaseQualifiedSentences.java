package fr.usubelli.compta;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.usubelli.compta.domain.nlp.QualifiedSentence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FileDatabaseQualifiedSentences {

    private final File qualifiedSentenceJsonFilePath;

    FileDatabaseQualifiedSentences(String path) {
        qualifiedSentenceJsonFilePath = new File(path);
    }

    List<QualifiedSentence> all() {
        try {
            return new ObjectMapper().readValue(qualifiedSentenceJsonFilePath, new TypeReference<List<QualifiedSentence>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
