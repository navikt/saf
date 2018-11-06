package no.nav.saf.anticorruptionlayer.pensjonSak;

import no.nav.saf.anticorruptionlayer.pensjonSak.domain.TilgangSak;
import no.nav.saf.anticorruptionlayer.pensjonSak.hentSakSammendragListe.SakSammendragConsumer;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.informasjon.WSSakSammendrag;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PensjonSakAntiCorruptionLayerImpl implements PensjonSakAntiCorruptionLayer {

	private final SakSammendragConsumer sakSammendragConsumer;

	@Inject
	public PensjonSakAntiCorruptionLayerImpl(SakSammendragConsumer sakSammendragConsumer) {
		this.sakSammendragConsumer = sakSammendragConsumer;
	}

	@Override
	public List<TilgangSak> hentSakSammendrag(final String personident) {

		List<WSSakSammendrag> sakSammendragListe = sakSammendragConsumer.hentSakSammendragListe(personident);
		return sakSammendragListe.stream()
				.map(psak -> TilgangSak.builder()
						.sakNr(psak.getSakId())
						.arkivSakSystem(Arkivsakssystem.PSAK)
						.tema(psak.getArkivtema().toString())
						.build())
				.collect(Collectors.toList());
	}
}
