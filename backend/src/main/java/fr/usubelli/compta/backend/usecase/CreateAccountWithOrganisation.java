package fr.usubelli.compta.backend.usecase;

import fr.usubelli.compta.backend.dto.CreateAccountWithOrganisationRequest;
import fr.usubelli.compta.backend.dto.Organisation;
import fr.usubelli.compta.backend.dto.OrganisationRights;
import fr.usubelli.compta.backend.dto.User;
import fr.usubelli.compta.backend.port.OrganisationGateway;
import fr.usubelli.compta.backend.port.UserGateway;

import java.util.Arrays;

public class CreateAccountWithOrganisation {

    private final UserGateway userGateway;
    private final OrganisationGateway organisationGateway;

    public CreateAccountWithOrganisation(UserGateway userGateway, OrganisationGateway organisationGateway) {
        this.userGateway = userGateway;
        this.organisationGateway = organisationGateway;
    }

    public CreateAccountWithOrganisationRequest createAccountWithOrganisation(CreateAccountWithOrganisationRequest request)
            throws UserAlreadyExistsException, OrganisationAlreadyExistsException {

        OrganisationRights rights = new OrganisationRights(request.getOrganisation().getSiren(), Arrays.asList(
                OrganisationRights.Right.UPDATE_ORGANISATION,
                OrganisationRights.Right.REDUCE_RIGHT,
                OrganisationRights.Right.GRANT_RIGHT));

        userGateway.createUser(new User(
                request.getEmail(),
                request.getPassword(),
                rights,
                User.UserState.CREATED));

        Organisation organisation = organisationGateway.createOrganisation(request.getOrganisation());

        return new CreateAccountWithOrganisationRequest(
                request.getEmail(),
                request.getPassword(),
                request.getFirstname(),
                request.getLastname(),
                request.getRole(),
                organisation);
    }

}
