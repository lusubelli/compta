package fr.usubelli.compta.backend.port;

import fr.usubelli.compta.backend.dto.Organization;
import fr.usubelli.compta.backend.exception.OrganisationAlreadyExistsException;

public interface OrganizationGateway {

    Organization createOrganisation(Organization organization) throws OrganisationAlreadyExistsException;

    Organization findOrganisation(String siren);

    Organization updateOrganisation(Organization organization);

}
