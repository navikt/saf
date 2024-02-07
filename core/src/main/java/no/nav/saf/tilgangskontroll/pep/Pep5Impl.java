package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.PEP5;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_DOKUMENT_METADATA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SKJERMING;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * Lokal caching er kun relevant for dokumentoversiktene og brukes i journalpostMapperDto.java
 */
@Slf4j
@Component(PEP5)
public class Pep5Impl extends Pep<TilgangDokumentInfo> {

	private final AbacService abacService;

	@Autowired
	public Pep5Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public AbacAnswer verifyAbacPdpDecision(TilgangDokumentInfo ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep5 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return AbacAnswer.deny(AbacAnswer.AbacDenyReasonCode.UKJENT);
		}

		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
		if (isSkjermingPresent(ressurs)) {
			XacmlResponse response = hasDokumentAccess(ressurs, safRequestContext);
			AbacAnswer abacAnswer = mapXacmlResponse(response);
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, abacAnswer);
			return abacAnswer;
		} else {
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, AbacAnswer.permit());
			return AbacAnswer.permit();
		}
	}

	@Override
	public AbacAnswer verifyAzureClientCredentialFlowAccess(TilgangDokumentInfo ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep5 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Azure ccf.");
			return AbacAnswer.deny(AbacAnswer.AbacDenyReason.builder()
					.cause("mangler_data").policy("saf_pep5").rule("dokument_info_er_null")
					.build());
		}
		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
		boolean decision = !isSkjermingPresent(ressurs);
		AbacAnswer abacAnswer = decision ? permit() : AbacAnswer.deny(AbacAnswer.AbacDenyReason.builder()
				.cause("dokument_info_skjermet").policy("saf_pep5").rule("dokument_info_skjermet")
				.build());
		safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, abacAnswer);
		return abacAnswer;
	}

	@Override
	AbacAnswer.AbacDenyReasonCode translateToDenyReasonCode(XacmlResponse xacmlResponse) {
		return AbacAnswer.AbacDenyReasonCode.SKJERMING;
	}

	private XacmlResponse hasDokumentAccess(TilgangDokumentInfo ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_DOKUMENT_METADATA);
		request.resource(RESOURCE_SAF_SKJERMING, ressurs.getSkjerming().name());

		traceLogPepStarted(PEP5, ressurs);
		XacmlResponse response = abacService.evaluate(request);
		traceLogPepFinished(PEP5, ressurs);

		return response;
	}

	private boolean isSkjermingPresent(TilgangDokumentInfo ressurs) {
		return ressurs.getSkjerming() != null;
	}
}
