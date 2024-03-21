package no.nav.saf.anticorruptionlayer.gsak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.gsak.hentgsaksaker.GsakConsumer;
import no.nav.saf.anticorruptionlayer.gsak.hentgsaksaker.GsakSakerTo;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.nav.saf.domain.DomainConstants.AKTOER_ID_LIST;
import static no.nav.saf.domain.DomainConstants.ORGNR_LIST;
import static no.nav.saf.domain.DomainConstants.TIDSSONE_NORGE;

@Slf4j
@Component
class GsakAntiCorruptionLayerImpl implements GsakAntiCorruptionLayer {
	private final GsakConsumer gsakConsumer;

	@Autowired
	public GsakAntiCorruptionLayerImpl(GsakConsumer gsakConsumer) {
		this.gsakConsumer = gsakConsumer;
	}

	@Override
	public List<Arkivsak> findArkivsakerByAktoerId(final List<String> aktoerIder, final List<Tema> tema) {
		try {
			if (aktoerIder.isEmpty() || tema.isEmpty()) {
				return new ArrayList<>();
			}
			List<GsakSakerTo> gsakSakerToFiltered = new ArrayList<>();
			if (tema.size() == 1) {
				gsakSakerToFiltered.addAll(gsakConsumer.hentSakerByAktoerIder(aktoerIder, tema.get(0)));
			} else {
				List<GsakSakerTo> gsakSakerTo = gsakConsumer.hentSakerByAktoerIder(aktoerIder);
				gsakSakerToFiltered.addAll(
						gsakSakerTo.stream()
								.filter(gsak -> tema.contains(mapTema(gsak.getTema())))
								.collect(Collectors.toList()));
			}

			return mapToArkivsak(gsakSakerToFiltered);
		} catch (Exception e) {
			log.warn("Klarte ikke hente gsaker for aktoerId", e);
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
								.filter(gsak -> tema.contains(mapTema(gsak.getTema())))
								.collect(Collectors.toList());
			}
			return mapToArkivsak(gsakSakerToFiltered);
		} catch (Exception e) {
			log.warn("Klarte ikke hente gsaker for orgnr={}", orgnr, e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<Arkivsak> findArkivsakerByAktoerId(String aktoerId) {
		try {
			if (aktoerId == null) {
				return new ArrayList<>();
			}

			return mapToArkivsak(gsakConsumer.hentSakerByAktoerId(aktoerId));
		} catch (Exception e) {
			log.warn("Klarte ikke hente gsaker for aktoerId", e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<Arkivsak> findArkivsakerByOrgnr(String orgnr) {
		try {
			if (orgnr == null) {
				return new ArrayList<>();
			}

			return mapToArkivsak(gsakConsumer.hentSakerByOrgNr(orgnr));

		} catch (Exception e) {
			log.warn("Klarte ikke hente gsaker for orgnr={}", orgnr, e);
			return new ArrayList<>();
		}
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
								.filter(gsak -> tema.contains(mapTema(gsak.getTema())))
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
						.fagsakId(gsak.getFagsakNr())
						.fagsaksystem(gsak.getApplikasjon())
						.tema(mapTema(gsak.getTema()))
						.datoOpprettet(gsak.getOpprettetTidspunkt().atZoneSameInstant(TIDSSONE_NORGE).toLocalDateTime())
						.build())
				.collect(Collectors.toList());
	}

	@Override
	public Map<String, List<String>> findIdListsByFagsakIdAndFagsaksystem(final String fagsakId, final String fagsaksystem) {
		List<GsakSakerTo> gsakSakerToList = gsakConsumer.hentSakerByFagsakIdAndFagsaksystem(fagsakId, fagsaksystem);

		if (gsakSakerToList == null || gsakSakerToList.isEmpty()) {
			return new HashMap<>();
		}

		List<String> aktoerIdList = new ArrayList<>();
		List<String> orgnrList = new ArrayList<>();
		gsakSakerToList.stream().forEach(gsakSakerTo -> {
			if (gsakSakerTo.getAktoerId() != null) {
				aktoerIdList.add(gsakSakerTo.getAktoerId());
			} else if (gsakSakerTo.getOrgnr() != null) {
				orgnrList.add(gsakSakerTo.getOrgnr());
			}
		});

		Map<String, List<String>> outMap = new HashMap<>();
		outMap.put(AKTOER_ID_LIST, aktoerIdList.stream().distinct().collect(Collectors.toList()));
		outMap.put(ORGNR_LIST, orgnrList.stream().distinct().collect(Collectors.toList()));

		return outMap;
	}

	private Tema mapTema(String tema) {
		if (tema == null) {
			return null;
		}
		try {
			return Tema.valueOf(tema.trim());
		} catch (Exception e) {
			return null;
		}
	}
}