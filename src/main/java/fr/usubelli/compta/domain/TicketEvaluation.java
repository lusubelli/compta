package fr.usubelli.compta.domain;

import fr.usubelli.compta.domain.nlp.Evaluation;
import fr.usubelli.compta.domain.nlp.IntentEvaluation;
import fr.usubelli.compta.domain.nlp.NameEvaluation;

public class TicketEvaluation {
    private final Evaluation evaluation;
    private final String text;

    public TicketEvaluation(Evaluation evaluation, String text) {
        this.evaluation = evaluation;
        this.text = text;
    }

    public String getType() {
        IntentEvaluation bestIntentEvaluation = null;
        for (IntentEvaluation intentEvaluation : this.evaluation.getIntentEvaluations()) {
            if (bestIntentEvaluation == null) {
                bestIntentEvaluation = intentEvaluation;
            } else {
                if (intentEvaluation.getProbability() > bestIntentEvaluation.getProbability()) {
                    bestIntentEvaluation = intentEvaluation;
                }
            }
        }
        if (bestIntentEvaluation == null) {
            return null;
        }
        return bestIntentEvaluation.getOutcome();
    }

    public String getTTC() {
        return getTextOfMatchingNameEvaluation(getBestMatchingNameEvaluation("TTC"));
    }

    public String getTVA() {
        return getTextOfMatchingNameEvaluation(getBestMatchingNameEvaluation("TVA"));
    }

    public String getDate() {
        return getTextOfMatchingNameEvaluation(getBestMatchingNameEvaluation("date"));
    }

    private String getTextOfMatchingNameEvaluation(NameEvaluation nameMatchingEvaluation) {
        if (nameMatchingEvaluation == null) {
            return null;
        }
        return text.replaceAll("\n", " ").substring(nameMatchingEvaluation.getStart(), nameMatchingEvaluation.getEnd());
    }

    private NameEvaluation getBestMatchingNameEvaluation(String label) {
        NameEvaluation bestMatchingNameEvaluation = null;
        for (NameEvaluation nameEvaluation : evaluation.getNameEvaluations()) {
            if (nameEvaluation.getType().equals(label)) {
                if (bestMatchingNameEvaluation == null) {
                    bestMatchingNameEvaluation = nameEvaluation;
                } else {
                    if (nameEvaluation.getProbability() > bestMatchingNameEvaluation.getProbability()) {
                        bestMatchingNameEvaluation = nameEvaluation;
                    }
                }
            }
        }
        return bestMatchingNameEvaluation;
    }

}
