package no.nav.saf.context.aktoerId.hentIdentForAktoerId;

import no.nav.saf.context.aktoerId.domain.HentIdentForAktoerIdResponseTo;
import no.nav.saf.context.exceptions.SafFunctionalException;
import no.nav.saf.context.exceptions.SafTechnicalException;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentIdentForAktoerIdPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdResponse;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class HentIdentForAktoerId {

	private final AktoerV2 aktoerV2;

	public HentIdentForAktoerId(AktoerV2 aktoerV2) {
		this.aktoerV2 = aktoerV2;
	}

	//	TODO Retry
	public HentIdentForAktoerIdResponseTo hentIdentForAktoerId(String aktoerId) {
		HentIdentForAktoerIdRequest request = new HentIdentForAktoerIdRequest();
		request.setAktoerId(aktoerId);
		try {
			HentIdentForAktoerIdResponse response = aktoerV2.hentIdentForAktoerId(request);
			return HentIdentForAktoerIdResponseTo.builder()
					.ident(response.getIdent())
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

