package no.nav.saf.anticorruptionlayer.aktoerid.hentidentforaktoerid;

import static no.nav.saf.anticorruptionlayer.RetryConstants.DELAY_SHORT_AKTOER_V2;
import static no.nav.saf.anticorruptionlayer.RetryConstants.MAX_ATTEMPTS_SHORT_AKTOER_V2;
import static no.nav.saf.anticorruptionlayer.RetryConstants.MULTIPLIER_SHORT_AKTOER_V2;

import no.nav.saf.anticorruptionlayer.aktoerid.domain.HentIdentForAktoerIdResponseTo;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.metrics.DokConsumerMetrics;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentIdentForAktoerIdPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */

@Component
public class HentIdentForAktoerId {

	private final AktoerV2 aktoerV2;

	public HentIdentForAktoerId(AktoerV2 aktoerV2) {
		this.aktoerV2 = aktoerV2;
	}

	@Retryable(include = SafTechnicalException.class,
			maxAttempts = MAX_ATTEMPTS_SHORT_AKTOER_V2,
			backoff = @Backoff(delay = DELAY_SHORT_AKTOER_V2, multiplier = MULTIPLIER_SHORT_AKTOER_V2))
	@DokConsumerMetrics(value = "dok_consumer", description = "hentIdentForAktoerId")
	public HentIdentForAktoerIdResponseTo hentIdentForAktoerId(String aktoerId) {
		HentIdentForAktoerIdRequest request = new HentIdentForAktoerIdRequest();
		request.setAktoerId(aktoerId);
		try {
			HentIdentForAktoerIdResponse response = aktoerV2.hentIdentForAktoerId(request);
			return HentIdentForAktoerIdResponseTo.builder()
					.foedselsnr(response.getIdent())
					.historiskeIdenter(response.getHistoriskeIdenter())
					.build();
		} catch (HentIdentForAktoerIdPersonIkkeFunnet e) {
			throw new SafFunctionalException(String.format("Ident ikke funnet for aktoerId=%s", aktoerId), e);
		} catch (Exception e) {
			throw new SafTechnicalException(String.format("Teknisk feil mot aktoerV2:HentIdentForAktoerId. AktoerId=%s. Feilmelding=%s", aktoerId, e
					.getMessage()), e);
		}
	}
}
