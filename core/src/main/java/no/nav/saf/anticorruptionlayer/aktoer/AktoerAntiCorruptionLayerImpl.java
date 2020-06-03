package no.nav.saf.anticorruptionlayer.aktoer;

import no.nav.saf.anticorruptionlayer.aktoer.aktoerv2.AktoerV2Consumer;
import no.nav.saf.anticorruptionlayer.aktoer.domain.HentAktoerIdForIdentResponseTo;
import no.nav.saf.anticorruptionlayer.aktoer.domain.HentIdentForAktoerIdListeResponseTo;
import no.nav.saf.anticorruptionlayer.aktoer.domain.HentIdentForAktoerIdResponseTo;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class AktoerAntiCorruptionLayerImpl implements AktoerAntiCorruptionLayer {
	private final AktoerV2Consumer aktoerV2Consumer;

	public AktoerAntiCorruptionLayerImpl(AktoerV2Consumer aktoerV2Consumer) {
		this.aktoerV2Consumer = aktoerV2Consumer;
	}

	@Override
	public TilgangBruker hentTilgangBrukerByAktoerId(String aktoerId) {
		if(isBlank(aktoerId)) {
			return TilgangBruker.builder()
					.aktoerId(aktoerId)
					.build();
		}

		HentIdentForAktoerIdResponseTo responseTo = aktoerV2Consumer.hentIdentForAktoerId(aktoerId);

		return TilgangBruker.builder()
				.foedselsnr(responseTo.getFoedselsnr())
				.aktoerId(aktoerId)
				.historiskeIdenter(responseTo.getHistoriskeIdenter().stream()
						.map(ident -> TilgangIdent.builder().identifikator(ident).build()).collect(Collectors.toList()))
				.build();
	}

	@Override
	public TilgangBruker hentTilgangBrukerByFoedselsnummer(String foedselsnummer) {
		if(isBlank(foedselsnummer)) {
			return TilgangBruker.builder()
					.foedselsnr(foedselsnummer)
					.build();
		}

		HentAktoerIdForIdentResponseTo responseTo = aktoerV2Consumer.hentAktoerIdForIdent(foedselsnummer);

		return TilgangBruker.builder()
				.foedselsnr(foedselsnummer)
				.aktoerId(responseTo.getAktoerId())
				.historiskeIdenter(responseTo.getHistoriskeIdenter().stream()
						.filter(historiskIdent -> !foedselsnummer.equals(historiskIdent))
						.map(ident -> TilgangIdent.builder().identifikator(ident).build()).collect(Collectors.toList()))
				.build();
	}

	@Override
	public List<TilgangBruker> hentTilgangBrukerListByAktoerIdList(List<String> aktoerIdList) {
		if (aktoerIdList.isEmpty()) {
			return new ArrayList<>();
		}

		List<HentIdentForAktoerIdListeResponseTo> responseTo = aktoerV2Consumer.hentIdentForAktoerIdListe(aktoerIdList);

		return responseTo.stream()
				.map(to -> TilgangBruker.builder()
						.foedselsnr(to.getFoedselsnr())
						.aktoerId(to.getAktoerId())
						.historiskeIdenter(to.getHistoriskeIdenter().stream()
								.map(ident -> TilgangIdent.builder().identifikator(ident).build())
								.collect(Collectors.toList()))
						.build())
				.collect(Collectors.toList());
	}


}