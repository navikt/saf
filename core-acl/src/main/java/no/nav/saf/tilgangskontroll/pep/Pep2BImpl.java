package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component("pep2b")
public class Pep2BImpl implements Pep<TilgangSak> {

	private final AbacService abacService;

	@Inject
	public Pep2BImpl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
//		TODO: Uncomment when implemented
//		if (ressurs == null) {
//			log.warn("Pep2b mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
//			return false;
//		}
		XacmlRequest request = new XacmlRequest();
		//TODO Populate request and perform call to pdp

		return true;
	}
}
