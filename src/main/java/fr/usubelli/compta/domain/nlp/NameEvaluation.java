package fr.usubelli.compta.domain.nlp;

public class NameEvaluation {

    private final String type;
    private final int start;
    private final int end;
    private final double probability;

    public NameEvaluation(String type, int start, int end, double probability) {
        this.type = type;
        this.start = start;
        this.end = end;
        this.probability = probability;
    }

    public String getType() {
        return type;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public double getProbability() {
        return probability;
    }

    @Override
    public String toString() {
        return "NameEvaluation{" +
                "type='" + type + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", probability=" + probability +
                '}';
    }

}