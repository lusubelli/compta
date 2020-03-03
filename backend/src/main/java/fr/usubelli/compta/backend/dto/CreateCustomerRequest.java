package fr.usubelli.compta.backend.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateCustomerRequest {

    private final String siren;
    private final Organisation organisation;
    private final String billingContactEmail;

    @JsonCreator
    public CreateCustomerRequest(
            @JsonProperty("siren") String siren,
            @JsonProperty("organisation") Organisation organisation,
            @JsonProperty("billingContactEmail") String billingContactEmail) {
        this.siren = siren;
        this.organisation = organisation;
        this.billingContactEmail = billingContactEmail;
    }

    public String getSiren() {
        return siren;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public String getBillingContactEmail() {
        return billingContactEmail;
    }

}
