package fr.usubelli.compta.backend.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateAccountWithOrganisationRequest {

    private final String email;
    private final String password;
    private final String firstname;
    private final String lastname;
    private final String role;
    private final Organisation organisation;

    @JsonCreator
    public CreateAccountWithOrganisationRequest(
            @JsonProperty("email") String email,
            @JsonProperty("password") String password,
            @JsonProperty("firstname") String firstname,
            @JsonProperty("lastname") String lastname,
            @JsonProperty("role") String role,
            @JsonProperty("organisation") Organisation organisation) {
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.role = role;
        this.organisation = organisation;
    }


    public Organisation getOrganisation() {
        return organisation;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getRole() {
        return role;
    }

}
