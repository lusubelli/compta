package fr.usubelli.compta.organisation;

public class Customer {

    private String siren;
    private String billingContactEmail;

    public Customer() {
        this.siren = null;
        this.billingContactEmail = null;
    }

    public String getSiren() {
        return siren;
    }

    public String getBillingContactEmail() {
        return billingContactEmail;
    }

    public void setSiren(String siren) {
        this.siren = siren;
    }

    public void setBillingContactEmail(String billingContactEmail) {
        this.billingContactEmail = billingContactEmail;
    }

}

