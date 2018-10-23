package no.nav.saf.coordinator;

import no.nav.saf.domain.visningsmodell.Bruker;

/**
 * Returnerer visningsmodell.
 * Kun lov å kalle dette interfacet fra grensesnittet.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface SafDomainCoordinator {
	Bruker findBrukerByAktoerId(String aktoerId);
}
