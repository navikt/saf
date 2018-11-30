package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component("pep5")
public class Pep5Impl implements Pep<TilgangDokumentInfo> {

	private final AbacService abacService;

	@Inject
	public Pep5Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangDokumentInfo ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep5 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return false;
		}

		XacmlRequest request = new XacmlRequest();
		//TODO Populate request and perform call to pdp

		return true;
	}
}
