package no.nav.saf.anticorruptionlayer.gsak;

import no.nav.saf.anticorruptionlayer.gsak.domain.GsakSakerTo;
import no.nav.saf.anticorruptionlayer.gsak.hentgsaksaker.GsakConsumer;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class GsakAntiCorruptionLayerImpl implements GsakAntiCorruptionLayer {
	private final GsakConsumer gsakConsumer;

	@Inject
	public GsakAntiCorruptionLayerImpl(GsakConsumer gsakConsumer) {
		this.gsakConsumer = gsakConsumer;
	}

	@Override
	public List<Sak> findSakerByAktoerId(final String aktoerId, final List<Tema> temaFilter) {

		List<GsakSakerTo> gsakSakerToFiltered;

		if (temaFilter.size() == 1) {
			gsakSakerToFiltered = gsakConsumer.hentSakerByAktoerId(aktoerId, temaFilter.get(0));

		} else {
			List<GsakSakerTo> gsakSakerTo = gsakConsumer.hentSakerByAktoerId(aktoerId);
			gsakSakerToFiltered =
					gsakSakerTo.stream()
							.filter(gsak -> temaFilter.contains(TemakodeValueOf(gsak.getTema())))
							.collect(Collectors.toList());

		}
		return gsakSakerToFiltered.stream()
				.map(gsak -> Sak.builder()
						.arkivsaksnummer(gsak.getId().toString())
						.arkivsaksystem(Arkivsakssystem.GSAK)
						.fagsaksnummer(gsak.getFagsakNr())
						.fagsystem(gsak.getApplikasjon())
						.tema(Tema.valueOf(gsak.getTema()))
						.datoOpprettet(gsak.getOpprettetTidspunkt().toLocalDateTime())
						.build())
				.collect(Collectors.toList());
	}

	private Tema TemakodeValueOf(String tema) {
		try {
			return Tema.valueOf(tema);
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
						.arkivsaksystem(Arkivsakssystem.GSAK)
						.fagsaksnummer(gsak.getFagsakNr())
						.fagsystem(gsak.getApplikasjon())
						.tema(Tema.valueOf(gsak.getTema()))
						.datoOpprettet(gsak.getOpprettetTidspunkt().toLocalDateTime())
						.build())
				.collect(Collectors.toList());
	}


	@Override
	public List<TilgangSak> findTilgangSakListByAktoerId(final String aktoerId) {
		List<GsakSakerTo> gsakSakerTo = gsakConsumer.hentSakerByAktoerId(aktoerId);

		return gsakSakerTo.stream()
				.map(gsak -> TilgangSak.builder()
						.arkivsaksnummer(gsak.getId().toString())
						.arkivsaksystem(Arkivsakssystem.GSAK.name())
						.tema(gsak.getTema())
						.build())
				.collect(Collectors.toList());
	}

	@Override
	public TilgangBruker findTilgangSakBySakId(final String sakId) {
		GsakSakerTo gsakSakerTo = gsakConsumer.hentSakBySakId(sakId);
		return gsakSakerTo == null ? null : TilgangBruker.builder()
				.aktoerId(gsakSakerTo.getAktoerId())
				.build();
	}
}
