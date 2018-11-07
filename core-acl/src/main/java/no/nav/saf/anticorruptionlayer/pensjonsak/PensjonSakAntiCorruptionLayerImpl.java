package no.nav.saf.anticorruptionlayer.pensjonsak;

import no.nav.saf.anticorruptionlayer.pensjonsak.hentsaksammendragliste.PensjonSakConsumer;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PensjonSakAntiCorruptionLayerImpl implements PensjonSakAntiCorruptionLayer {

	private final PensjonSakConsumer pensjonSakConsumer;

	@Inject
	public PensjonSakAntiCorruptionLayerImpl(PensjonSakConsumer pensjonSakConsumer) {
		this.pensjonSakConsumer = pensjonSakConsumer;
	}

	@Override
	public List<TilgangSak> hentTilgangSakList(final String personident) {

		return pensjonSakConsumer.hentSakSammendragListe(personident).getSakSammendragListe().stream()
				.map(tilgangsak -> TilgangSak.builder()
						.arkivsaksnummer(tilgangsak.getSakNr())
						.arkivsaksystem(String.valueOf(Arkivsakssystem.PSAK))
						.tema(tilgangsak.getTema())
						.build())
				.collect(Collectors.toList());
	}
}
