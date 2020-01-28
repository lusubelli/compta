package fr.usubelli.compta.domain.nlp;

public class IntentEvaluation {

    private final String outcome;
    private final double probability;

    public IntentEvaluation(String outcome, double probability) {
        this.outcome = outcome;
        this.probability = probability;
    }

    public double getProbability() {
        return probability;
    }

    public String getOutcome() {
        return outcome;
    }

    @Override
    public String toString() {
        return "IntentEvaluation{" +
                "outcome='" + outcome + '\'' +
                ", probability=" + probability +
                '}';
    }

}
