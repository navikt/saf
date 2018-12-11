package no.nav.saf.anticorruptionlayer.gsak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.gsak.hentgsaksaker.GsakConsumer;
import no.nav.saf.anticorruptionlayer.gsak.hentgsaksaker.GsakSakerTo;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
class GsakAntiCorruptionLayerImpl implements GsakAntiCorruptionLayer {
	private final GsakConsumer gsakConsumer;

	@Inject
	public GsakAntiCorruptionLayerImpl(GsakConsumer gsakConsumer) {
		this.gsakConsumer = gsakConsumer;
	}

	@Override
	public List<Arkivsak> findArkivsakerByAktoerId(final String aktoerId, final List<Tema> tema) {
		try {
			List<GsakSakerTo> gsakSakerToFiltered;

			if (aktoerId == null || tema.isEmpty()) {
				return new ArrayList<>();
			} else if (tema.size() == 1) {
				gsakSakerToFiltered = gsakConsumer.hentSakerByAktoerId(aktoerId, tema.get(0));
			} else {
				List<GsakSakerTo> gsakSakerTo = gsakConsumer.hentSakerByAktoerId(aktoerId);
				gsakSakerToFiltered =
						gsakSakerTo.stream()
								.filter(gsak -> tema.contains(mapToTema(gsak.getTema())))
								.collect(Collectors.toList());
			}

			return mapToArkivsak(gsakSakerToFiltered);
		} catch (Exception e) {
			log.warn("Klarte ikke hente gsaker for aktoerId={}", aktoerId, e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<Arkivsak> findArkivsakerByOrgnr(final String orgnr, final List<Tema> tema) {
		try {
			List<GsakSakerTo> gsakSakerToFiltered;

			if (orgnr == null || tema.isEmpty()) {
				return new ArrayList<>();
			} else if (tema.size() == 1) {
				gsakSakerToFiltered = gsakConsumer.hentSakerByOrgNr(orgnr, tema.get(0));
			} else {
				List<GsakSakerTo> gsakSakerTo = gsakConsumer.hentSakerByOrgNr(orgnr);
				gsakSakerToFiltered =
						gsakSakerTo.stream()
								.filter(gsak -> tema.contains(mapToTema(gsak.getTema())))
								.collect(Collectors.toList());
			}

			return mapToArkivsak(gsakSakerToFiltered);
		} catch (Exception e) {
			log.warn("Klarte ikke hente gsaker for orgnr={}", orgnr, e);
			return new ArrayList<>();
		}
	}

	private Tema mapToTema(String tema) {
		try {
			return Tema.valueOf(tema);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public TilgangSak findTilgangSakBySakId(final String sakId) {
		GsakSakerTo gsakSakerTo = gsakConsumer.hentSakBySakId(sakId);
		return gsakSakerTo == null ? null : TilgangSak.builder()
				.aktoerId(gsakSakerTo.getAktoerId())
				.arkivsaksnummer(gsakSakerTo.getId().toString())
				.arkivsaksystem(Arkivsakssystem.GSAK.name())
				.fagsystem(gsakSakerTo.getApplikasjon())
				.fagsaknummer(gsakSakerTo.getFagsakNr())
				.tema(gsakSakerTo.getTema())
				.orgnummer(gsakSakerTo.getOrgnr())
				.build();
	}

	@Override
	public List<Arkivsak> findTilgangSakListByFagsakIdAndFagsaksystem(final String fagsakId, final String fagsaksystem, final List<Tema> tema) {
		try {
			List<GsakSakerTo> gsakSakerToFiltered;
			if (tema.isEmpty()) {
				return new ArrayList<>();
			} else {
				List<GsakSakerTo> gsakSakerTo = gsakConsumer.hentSakerByFagsakIdAndFagsaksystem(fagsakId, fagsaksystem);
				gsakSakerToFiltered =
						gsakSakerTo.stream()
								.filter(gsak -> tema.contains(mapToTema(gsak.getTema())))
								.collect(Collectors.toList());
			}
			return mapToArkivsak(gsakSakerToFiltered);
		} catch (Exception e) {
			log.warn("Klarte ikke hente gsaker for fagsakId={}, fagsaksystem={}", fagsakId, fagsaksystem, e);
			return new ArrayList<>();
		}
	}

	private List<Arkivsak> mapToArkivsak(List<GsakSakerTo> gsakSakerToFiltered) {
		return gsakSakerToFiltered.stream()
				.map(gsak -> Arkivsak.builder()
						.aktoerId(gsak.getAktoerId())
						.orgnummer(gsak.getOrgnr())
						.arkivsaksnummer(gsak.getId().toString())
						.arkivsaksystem(Arkivsakssystem.GSAK)
						.fagsaksnummer(gsak.getFagsakNr())
						.fagsaksystem(gsak.getApplikasjon())
						.tema(Tema.valueOf(gsak.getTema()))
						.datoOpprettet(gsak.getOpprettetTidspunkt().toLocalDateTime())
						.build())
				.collect(Collectors.toList());
	}

	@Override
	public List<String> findAktoerIdListByFagsakIdAndFagsaksystem(final String fagsakId, final String fagsaksystem) {
		List<GsakSakerTo> gsakSakerToList = gsakConsumer.hentSakerByFagsakIdAndFagsaksystem(fagsakId, fagsaksystem);
		return gsakSakerToList == null || gsakSakerToList.isEmpty() ? new ArrayList<>() :
				gsakSakerToList.stream()
						.map(GsakSakerTo::getAktoerId)
						.distinct()
						.collect(Collectors.toList());
	}

	@Override
	public TilgangBruker findTilgangBrukerBySakId(final String sakId) {
		GsakSakerTo gsakSakerTo = gsakConsumer.hentSakBySakId(sakId);
		return gsakSakerTo == null ? null : TilgangBruker.builder()
				.aktoerId(gsakSakerTo.getAktoerId())
				.build();
	}
}
