package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_TREDJEPART;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/Tilgang+til+paragraf19
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component("pep3")
public class Pep3Impl implements Pep<TilgangSak> {

	private final AbacService abacService;

	@Inject
	public Pep3Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep3 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return false;
		}

		if (hasMetadataAccess(ressurs)) {
			if (hasNotRelevanteTredjeparter(ressurs)) {
				return true;
			}
			XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext().getOidcTokenBody());
			request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART);
			ressurs.getRelevanteTredjeparter().stream()
					.forEach(tilgangRelevantTredjepart -> request.resource(RESOURCE_FELLES_PERSON_FNR, tilgangRelevantTredjepart
							.getIdent().getIdentifikator()));

			if (log.isTraceEnabled()) {
				log.trace("Pep3 evaluerer arkivsak={}, arkivsaksystem={}, tema={}", ressurs.getArkivsaksnummer(), ressurs.getArkivsaksystem(), ressurs
						.getTema());
			}

			XacmlResponse response = abacService.evaluate(request);

			if (log.isTraceEnabled()) {
				log.trace("Pep3 ferdig evaluert arkivsak={}, arkivsaksystem={}, tema={}", ressurs.getArkivsaksnummer(), ressurs.getArkivsaksystem(), ressurs
						.getTema());
			}
			return Decision.PERMIT.equals(response.getDecision());
		} else {
			return true;
		}
	}

	private boolean hasNotRelevanteTredjeparter(TilgangSak ressurs) {
		return ressurs.getRelevanteTredjeparter() == null || ressurs.getRelevanteTredjeparter().isEmpty();
	}

	private boolean hasMetadataAccess(TilgangSak ressurs) {
		return isFarskapSak(ressurs) || isBidragSak(ressurs);
	}

	private boolean isFarskapSak(TilgangSak ressurs) {
		return Tema.FAR.name().equals(ressurs.getTema());
	}

	private boolean isBidragSak(TilgangSak ressurs) {
		return Tema.BID.name().equals(ressurs.getTema());
	}
}
