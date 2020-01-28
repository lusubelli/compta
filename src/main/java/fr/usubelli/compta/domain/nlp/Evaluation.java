package fr.usubelli.compta.domain.nlp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Evaluation {

    private final List<IntentEvaluation> intentEvaluations;
    private final List<NameEvaluation> nameEvaluations;

    @JsonCreator
    public Evaluation(
            @JsonProperty("intents") List<IntentEvaluation> intentEvaluations,
            @JsonProperty("names") List<NameEvaluation> nameEvaluations) {
        this.intentEvaluations = intentEvaluations;
        this.nameEvaluations = nameEvaluations;
    }

    public List<IntentEvaluation> getIntentEvaluations() {
        return intentEvaluations;
    }

    public List<NameEvaluation> getNameEvaluations() {
        return nameEvaluations;
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "intentEvaluations=" + intentEvaluations +
                ", nameEvaluations=" + nameEvaluations +
                '}';
    }

}
