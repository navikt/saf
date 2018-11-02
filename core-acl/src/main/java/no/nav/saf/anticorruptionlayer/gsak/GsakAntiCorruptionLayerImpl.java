package no.nav.saf.anticorruptionlayer.gsak;

import no.nav.saf.anticorruptionlayer.gsak.domain.GsakSakerTo;
import no.nav.saf.anticorruptionlayer.gsak.hentgsaksaker.GsakConsumer;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.Tema;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class GsakAntiCorruptionLayerImpl implements GsakAntiCorruptionLayer {
	private final GsakConsumer gsakConsumer;

	@Inject
	public GsakAntiCorruptionLayerImpl(GsakConsumer gsakConsumer) {
		this.gsakConsumer = gsakConsumer;
	}

	@Override
	public Set<Tema> findTemaerByAktoerIdAndFilterTemakode(final String aktoerId, final List<Temakode> temakoder) {

		List<GsakSakerTo> gsakSakerTo = gsakConsumer.hentSakerByAktoerId(aktoerId);
		return gsakSakerTo.stream().map(gsak -> Tema.fromTemakode(Temakode.valueOf(gsak.getTema())))
				.filter(t -> temakoder.isEmpty() || temakoder.contains(t.getTema()))
				.collect(Collectors.toSet());
	}

	@Override
	public List<Sak> findSakerByAktoerId(final String aktoerId, final List<Temakode> temakodeFilter) {

		List<GsakSakerTo> gsakSakerToFiltered;

		if (temakodeFilter.size() == 1) {
			gsakSakerToFiltered = gsakConsumer.hentSakerByAktoerId(aktoerId, temakodeFilter.get(0));

		} else {
			List<GsakSakerTo> gsakSakerTo = gsakConsumer.hentSakerByAktoerId(aktoerId);
			gsakSakerToFiltered =
					gsakSakerTo.stream()
							.filter(gsak -> temakodeFilter.contains(TemakodeValueOf(gsak.getTema())))
							.collect(Collectors.toList());

		}
		return gsakSakerToFiltered.stream()
				.map(gsak -> Sak.builder()
						.arkivsaksnummer(gsak.getId().toString())
						.arkivsakssystem(Arkivsakssystem.GSAK)
						.fagsaksnummer(gsak.getFagsakNr())
						.fagsystem(gsak.getApplikasjon())
						.temakode(Temakode.valueOf(gsak.getTema()))
						.datoOpprettet(gsak.getOpprettetTidspunkt())
						.build())
				.collect(Collectors.toList());
	}

	private Temakode TemakodeValueOf(String tema) {
		try {
			return Temakode.valueOf(tema);
		} catch (Exception e) {
			return null;
		}
	}


	@Override
	public List<Sak> findSakerByAktoerId(final String aktoerId) {

		List<GsakSakerTo> gsakSakerTo = gsakConsumer.hentSakerByAktoerId(aktoerId);


		return gsakSakerTo.stream()
				.map(gsak -> Sak.builder()
						.arkivsaksnummer(gsak.getId().toString())
						.arkivsakssystem(Arkivsakssystem.GSAK)
						.fagsaksnummer(gsak.getFagsakNr())
						.fagsystem(gsak.getApplikasjon())
						.temakode(Temakode.valueOf(gsak.getTema()))
						.datoOpprettet(gsak.getOpprettetTidspunkt())
						.build())
				.collect(Collectors.toList());
	}

}
