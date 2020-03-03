package fr.usubelli.compta.organisation.port;

import fr.usubelli.compta.organisation.Organisation;
import fr.usubelli.compta.organisation.OrganisationAlreadyExistsException;
import fr.usubelli.compta.organisation.OrganisationNotFoundException;

public interface OrganisationRepository {

    Organisation createOrganisation(Organisation organisation) throws OrganisationAlreadyExistsException;

    Organisation updateOrganisation(Organisation organisation) throws OrganisationNotFoundException;
}
