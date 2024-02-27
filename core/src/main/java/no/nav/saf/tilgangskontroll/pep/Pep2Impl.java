package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import no.nav.saf.tilgangskontroll.pep.reasons.TemaReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.kode.Tema.FAR;
import static no.nav.saf.domain.kode.Tema.KTA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_TEMA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SAK_JP_METADATA;
import static no.nav.saf.tilgangskontroll.abac.dto.response.AdviceStringUtil.getAdvicesMap;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/Tilgang+til+farskapssaker
 */
@Slf4j
@Component(PEP2)
public class Pep2Impl extends Pep<TilgangSak> {

	private final AbacService abacService;

	@Autowired
	public Pep2Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public AbacAnswer verifyAbacPdpDecision(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.error("Pep2 (tema FAR eller KTA) mangler data om journalposten. Den må ha tema for å gjøre tilgangskontroll. Dette er forårsaket av en teknisk feil");
			return AbacAnswer.deny(new UkjentEllerTekniskReason());
		}

		if (isFarskap(ressurs) || isKontrollAnmeldelse(ressurs)) {
			XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
			request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_JP_METADATA);
			request.resource(RESOURCE_FELLES_TEMA, ressurs.getTema().name());

			traceLogPepStarted(PEP2, ressurs);
			XacmlResponse response = abacService.evaluate(request);
			traceLogPepFinished(PEP2, ressurs);

			return response.isPermit() ? AbacAnswer.permit() : AbacAnswer.deny(new TemaReason(getAdvicesMap(response.getAdvices()), ressurs.getTema()));

		} else {
			return AbacAnswer.permit();
		}
	}

	@Override
	public AbacAnswer verifyAzureClientCredentialFlowAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.error("Pep2 (tema FAR eller KTA) mangler data om journalposten. Den må ha tema for å gjøre tilgangskontroll. Dette er forårsaket av en teknisk feil");
			return AbacAnswer.deny(new TemaReason(
					"cause_0013_ikketilgangtiltema", "saf_pep2", "mangler_tema", null
			));
		}
		String tema = ressurs.getTema().name().toLowerCase();
		if (isFarskap(ressurs)) {
			return safRequestContext.getSecurityContext().hasTemaAzureRole(tema) ?
					permit() : AbacAnswer.deny(new TemaReason(
					"cause_0013_ikketilgangtiltema", "saf_farskap", "tematilgang_nok", FAR
			));
		} else if (isKontrollAnmeldelse(ressurs)) {
			return safRequestContext.getSecurityContext().hasTemaAzureRole(tema) ?
					permit() : AbacAnswer.deny(new TemaReason(
					"cause_0013_ikketilgangtiltema", "saf_kontrollanmeldelse", "tematilgang_nok", KTA
			));
		} else {
			return permit();
		}
	}

	private boolean isFarskap(TilgangSak ressurs) {
		return FAR.equals(ressurs.getTema());
	}

	private boolean isKontrollAnmeldelse(TilgangSak ressurs) {
		return KTA.equals(ressurs.getTema());
	}

}
