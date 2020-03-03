package fr.usubelli.compta.backend.port;

import fr.usubelli.compta.backend.dto.Organisation;
import fr.usubelli.compta.backend.usecase.OrganisationAlreadyExistsException;

public interface OrganisationGateway {

    Organisation createOrganisation(Organisation organisation) throws OrganisationAlreadyExistsException;

    Organisation findOrganisation(String siren);

    Organisation updateOrganisation(Organisation organisation);

}
