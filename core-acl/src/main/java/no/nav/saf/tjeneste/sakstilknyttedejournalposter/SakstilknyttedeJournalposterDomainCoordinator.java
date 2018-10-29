package no.nav.saf.tjeneste.sakstilknyttedejournalposter;

import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.Bruker;

/**
 * Returnerer visningsmodell.
 * Kun lov å kalle dette interfacet fra grensesnittet.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface SakstilknyttedeJournalposterDomainCoordinator {
	Bruker findBrukerByAktoerId(String aktoerId);
}
