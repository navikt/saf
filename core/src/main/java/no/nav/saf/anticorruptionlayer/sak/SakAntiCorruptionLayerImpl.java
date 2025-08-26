package no.nav.saf.anticorruptionlayer.sak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.sak.hentsaksaker.SakConsumer;
import no.nav.saf.anticorruptionlayer.sak.hentsaksaker.SakSakerTo;
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
class SakAntiCorruptionLayerImpl implements SakAntiCorruptionLayer {

	private final SakConsumer sakConsumer;

	@Autowired
	public SakAntiCorruptionLayerImpl(SakConsumer sakConsumer) {
		this.sakConsumer = sakConsumer;
	}

	@Override
	public List<Arkivsak> findArkivsakerByAktoerId(final List<String> aktoerIder, final List<Tema> tema) {
		try {
			if (aktoerIder.isEmpty() || tema.isEmpty()) {
				return new ArrayList<>();
			}
			List<SakSakerTo> sakSakerToFiltered = new ArrayList<>();
			if (tema.size() == 1) {
				sakSakerToFiltered.addAll(sakConsumer.hentSakerByAktoerIder(aktoerIder, tema.get(0)));
			} else {
				List<SakSakerTo> sakSakerTo = sakConsumer.hentSakerByAktoerIder(aktoerIder);
				sakSakerToFiltered.addAll(
						sakSakerTo.stream()
								.filter(gsak -> tema.contains(mapTema(gsak.getTema())))
								.toList());
			}

			return mapToArkivsak(sakSakerToFiltered);
		} catch (Exception e) {
			log.warn("Klarte ikke hente saker for aktoerId", e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<Arkivsak> findArkivsakerByOrgnr(final String orgnr, final List<Tema> tema) {
		try {
			List<SakSakerTo> sakSakerToFiltered;

			if (orgnr == null || tema.isEmpty()) {
				return new ArrayList<>();
			} else if (tema.size() == 1) {
				sakSakerToFiltered = sakConsumer.hentSakerByOrgNr(orgnr, tema.get(0));
			} else {
				List<SakSakerTo> sakSakerTo = sakConsumer.hentSakerByOrgNr(orgnr);
				sakSakerToFiltered =
						sakSakerTo.stream()
								.filter(gsak -> tema.contains(mapTema(gsak.getTema())))
								.collect(Collectors.toList());
			}
			return mapToArkivsak(sakSakerToFiltered);
		} catch (Exception e) {
			log.warn("Klarte ikke hente saker for orgnr={}", orgnr, e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<Arkivsak> findArkivsakerByAktoerId(String aktoerId) {
		try {
			if (aktoerId == null) {
				return new ArrayList<>();
			}

			return mapToArkivsak(sakConsumer.hentSakerByAktoerId(aktoerId));
		} catch (Exception e) {
			log.warn("Klarte ikke hente saker for aktoerId", e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<Arkivsak> findArkivsakerByOrgnr(String orgnr) {
		try {
			if (orgnr == null) {
				return new ArrayList<>();
			}

			return mapToArkivsak(sakConsumer.hentSakerByOrgNr(orgnr));

		} catch (Exception e) {
			log.warn("Klarte ikke hente saker for orgnr={}", orgnr, e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<Arkivsak> findTilgangSakListByFagsakIdAndFagsaksystem(final String fagsakId, final String fagsaksystem, final List<Tema> tema) {
		try {
			List<SakSakerTo> sakSakerToFiltered;
			if (tema.isEmpty()) {
				return new ArrayList<>();
			} else {
				List<SakSakerTo> sakSakerTo = sakConsumer.hentSakerByFagsakIdAndFagsaksystem(fagsakId, fagsaksystem);
				sakSakerToFiltered =
						sakSakerTo.stream()
								.filter(gsak -> tema.contains(mapTema(gsak.getTema())))
								.collect(Collectors.toList());
			}
			return mapToArkivsak(sakSakerToFiltered);
		} catch (Exception e) {
			log.warn("Klarte ikke hente saker for fagsakId={}, fagsaksystem={}", fagsakId, fagsaksystem, e);
			return new ArrayList<>();
		}
	}

	private List<Arkivsak> mapToArkivsak(List<SakSakerTo> sakSakerToFiltered) {
		return sakSakerToFiltered.stream()
				.map(gsak -> Arkivsak.builder()
						.aktoerId(gsak.getAktoerId())
						.orgnummer(gsak.getOrgnr())
						.arkivsaksnummer(gsak.getId().toString())
						.arkivsaksystem(Arkivsakssystem.GSAK)
						.avsluttet(Arkivsak.sakStatusIsAvsluttet(gsak.getSakStatus()))
						.fagsakId(gsak.getFagsakNr())
						.fagsaksystem(gsak.getApplikasjon())
						.tema(mapTema(gsak.getTema()))
						.datoOpprettet(gsak.getOpprettetTidspunkt().atZoneSameInstant(TIDSSONE_NORGE).toLocalDateTime())
						.build())
				.collect(Collectors.toList());
	}

	@Override
	public Map<String, List<String>> findIdListsByFagsakIdAndFagsaksystem(final String fagsakId, final String fagsaksystem) {
		List<SakSakerTo> sakSakerToList = sakConsumer.hentSakerByFagsakIdAndFagsaksystem(fagsakId, fagsaksystem);

		if (sakSakerToList == null || sakSakerToList.isEmpty()) {
			return new HashMap<>();
		}

		List<String> aktoerIdList = new ArrayList<>();
		List<String> orgnrList = new ArrayList<>();
		sakSakerToList.forEach(sakSakerTo -> {
			if (sakSakerTo.getAktoerId() != null) {
				aktoerIdList.add(sakSakerTo.getAktoerId());
			} else if (sakSakerTo.getOrgnr() != null) {
				orgnrList.add(sakSakerTo.getOrgnr());
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