package fr.usubelli.compta.organisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class Organisation {

    @JsonIgnore
    private ObjectId id;
    private String name;
    private String address;
    private String zipcode;
    private String town;
    private String siren;
    private String tva;
    private List<Customer> customers;

    public Organisation() {
        this.customers = new ArrayList<>();
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getZipcode() {
        return zipcode;
    }

    public String getTown() {
        return town;
    }

    public String getSiren() {
        return siren;
    }

    public String getTva() {
        return tva;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public void setSiren(String siren) {
        this.siren = siren;
    }

    public void setTva(String tva) {
        this.tva = tva;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

}
