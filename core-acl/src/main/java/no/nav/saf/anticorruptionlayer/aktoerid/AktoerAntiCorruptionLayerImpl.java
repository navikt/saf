package no.nav.saf.anticorruptionlayer.aktoerid;

import no.nav.saf.anticorruptionlayer.aktoerid.domain.HentIdentForAktoerIdResponseTo;
import no.nav.saf.anticorruptionlayer.aktoerid.hentidentforaktoerid.HentIdentForAktoerId;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class AktoerAntiCorruptionLayerImpl implements AktoerAntiCorruptionLayer {
	private final HentIdentForAktoerId hentIdentForAktoerId;

	public AktoerAntiCorruptionLayerImpl(HentIdentForAktoerId hentIdentForAktoerId) {
		this.hentIdentForAktoerId = hentIdentForAktoerId;
	}

	@Override
	public TilgangBruker hentTilgangBruker(String aktoerId) {
		HentIdentForAktoerIdResponseTo responseTo = hentIdentForAktoerId.hentIdentForAktoerId(aktoerId);

		return TilgangBruker.builder()
				.foedselsnr(responseTo.getFoedselsnr())
				.aktoerId(aktoerId)
				.historiskeIdenter(responseTo.getHistoriskeIdenter().stream()
						.map(ident -> TilgangIdent.builder().identifikator(ident).build()).collect(Collectors.toList()))
				.build();
	}
}