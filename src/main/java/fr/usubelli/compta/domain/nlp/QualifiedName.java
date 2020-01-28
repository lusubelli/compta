package fr.usubelli.compta.domain.nlp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QualifiedName {

    private final int start;
    private final int end;
    private final String role;

    @JsonCreator
    public QualifiedName(
            @JsonProperty("start") int start,
            @JsonProperty("end") int end,
            @JsonProperty("role") String role) {
        this.start = start;
        this.end = end;
        this.role = role;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getRole() {
        return role;
    }

}
