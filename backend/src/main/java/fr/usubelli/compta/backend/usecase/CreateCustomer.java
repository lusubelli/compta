package fr.usubelli.compta.backend.usecase;

import fr.usubelli.compta.backend.dto.Customer;
import fr.usubelli.compta.backend.dto.Organisation;
import fr.usubelli.compta.backend.port.OrganisationGateway;

import java.util.List;

public class CreateCustomer {

    private final OrganisationGateway organisationGateway;

    public CreateCustomer(OrganisationGateway organisationGateway) {
        this.organisationGateway = organisationGateway;
    }

    public Customer createCustomer(String siren, Organisation customerOrganisation, String billingContactEmail) {

        try {
            organisationGateway.createOrganisation(customerOrganisation);
        } catch (OrganisationAlreadyExistsException e) {}

        final Organisation ownOrganisation = organisationGateway.findOrganisation(siren);

        final List<Customer> customers = ownOrganisation.getCustomers();
        final Customer customer = new Customer(customerOrganisation.getSiren(), billingContactEmail);
        customers.add(customer);

        organisationGateway.updateOrganisation(new Organisation(
                ownOrganisation.getName(),
                ownOrganisation.getAddress(),
                ownOrganisation.getZipcode(),
                ownOrganisation.getTown(),
                ownOrganisation.getSiren(),
                ownOrganisation.getTva(),
                customers));

        return customer;
    }

}
