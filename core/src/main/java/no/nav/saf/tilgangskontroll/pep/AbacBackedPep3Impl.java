package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import no.nav.saf.tilgangskontroll.pep.reasons.EgenAnsattReason;
import no.nav.saf.tilgangskontroll.pep.reasons.FortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseUtlandReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_BISYS;
import static no.nav.saf.domain.DomainConstants.PEP3;
import static no.nav.saf.domain.kode.Tema.BID;
import static no.nav.saf.domain.kode.Tema.FAR;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_TREDJEPART;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.EGEN_ANSATT;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.FORTROLIG_ADRESSE;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.STRENGT_FORTROLIG_ADRESSE;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.STRENGT_FORTROLIG_ADRESSE_UTLAND;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.deny;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/FP1%3A+Behandling+Kode+6+Brukere
 * https://confluence.adeo.no/display/ABAC/FP2%3A+Behandling+Kode+7+Brukere
 */
@Slf4j
@Component
public class AbacBackedPep3Impl extends StandardAbacBackedPep<TilgangSak> {

	private static final EnumSet<Tema> RELEVANTE_TEMA = EnumSet.of(BID, FAR);
	private final AbacService abacService;

	@Autowired
	public AbacBackedPep3Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public PepAnswer verifyAbacPdpDecision(TilgangSak ressurs, SafRequestContext safRequestContext) {

		if (ressurs != null && RELEVANTE_TEMA.contains(ressurs.getTema()) && FAGSAKSYSTEM_BISYS.equals(ressurs.getFagsaksystem())) {

			if (ressurs.getRelevanteTredjeparter() == null || ressurs.getRelevanteTredjeparter().isEmpty()) {
				log.info("Pep3(relevante-parter) har ingen relevante parter. Tilgang gis.");
				return PepAnswer.permit();
			}

			List<TilgangRelevantTredjepart> relevantTredjeparter = ressurs.getRelevanteTredjeparter();

			XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());

			request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART);
			relevantTredjeparter.forEach(tilgangRelevantTredjepart -> request.resource(RESOURCE_FELLES_PERSON_FNR, tilgangRelevantTredjepart.getIdent().getIdentifikator()));

			traceLogPepStarted(PEP3, ressurs);
			XacmlResponse response = abacService.evaluate(request);
			traceLogPepFinished(PEP3, ressurs);

			return mapToAbacAnswer(response);
		}
		return PepAnswer.permit();
	}

	@Override
	public PepAnswer verifyAzureClientCredentialFlowAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		return permit();
	}

	@Override
	protected PepAnswer translateToDenyReasonCode(XacmlResponse xacmlResponse) {
		var advices = xacmlResponse.getAdvicesMap();

		if (FORTROLIG_ADRESSE.matchesAbacAdvice(advices)) {
			return deny(new FortroligAdresseReason(advices));
		} else if (STRENGT_FORTROLIG_ADRESSE.matchesAbacAdvice(advices)) {
			return deny(new StrengtFortroligAdresseReason(advices));
		} else if (STRENGT_FORTROLIG_ADRESSE_UTLAND.matchesAbacAdvice(advices)) {
			return deny(new StrengtFortroligAdresseUtlandReason(advices));
		} else if(EGEN_ANSATT.matchesAbacAdvice(advices)) {
			return deny(new EgenAnsattReason(advices));
		}
		log.warn("pep3 kunne ikke matche abac-response til DenyReason advices={}", advices);
		return deny(new UkjentEllerTekniskReason());
	}
}
