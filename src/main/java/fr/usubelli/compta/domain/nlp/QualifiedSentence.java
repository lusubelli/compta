package fr.usubelli.compta.domain.nlp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class QualifiedSentence {

    private final String intent;
    private final String text;
    private final List<QualifiedName> qualifiedNames;

    @JsonCreator
    public QualifiedSentence(
            @JsonProperty("intent") String intent,
            @JsonProperty("text") String text,
            @JsonProperty("names") List<QualifiedName> qualifiedNames) {
        this.intent = intent;
        this.text = text;
        this.qualifiedNames = qualifiedNames;
    }

    public String getIntent() {
        return intent;
    }

    public String getText() {
        return text;
    }

    public List<QualifiedName> getQualifiedNames() {
        return qualifiedNames;
    }

}
