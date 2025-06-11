package no.nav.saf.query.dokumentoversikt.fagsak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.PdlAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.fpsak.FpsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.k9.K9AntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.argumenter.FagsakInput;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayerImpl.PSAK_FAGSYSTEM;
import static no.nav.saf.cache.LokalCacheConfig.TILGANGSMODELL_REPO_BRUKER_CACHE;
import static no.nav.saf.domain.DomainConstants.AKTOER_ID_LIST;
import static no.nav.saf.domain.DomainConstants.ORGNR_LIST;

@Slf4j
@Component
class DokumentoversiktFagsakTilgangsmodellRepository {

	private final PdlAntiCorruptionLayer aktoerAntiCorruptionLayer;
	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;
	private final FpsakAntiCorruptionLayer fpsakAntiCorruptionLayer;
	private final K9AntiCorruptionLayer k9AntiCorruptionLayer;

	public DokumentoversiktFagsakTilgangsmodellRepository(
			PdlAntiCorruptionLayer aktoerAntiCorruptionLayer,
			GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
			PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
			BisysAntiCorruptionLayer bisysAntiCorruptionLayer,
			FpsakAntiCorruptionLayer fpsakAntiCorruptionLayer,
			K9AntiCorruptionLayer k9AntiCorruptionLayer
	) {
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
		this.fpsakAntiCorruptionLayer = fpsakAntiCorruptionLayer;
		this.k9AntiCorruptionLayer = k9AntiCorruptionLayer;
	}

	@Cacheable(cacheNames = TILGANGSMODELL_REPO_BRUKER_CACHE, unless = "#fagsakInput.fagsaksystem == 'BISYS'")
	public List<TilgangBruker> findTilgangBrukerList(FagsakInput fagsakInput) {
		try {
			if (fagsakInput.getFagsaksystem().equals(PSAK_FAGSYSTEM)) {
				return findTilgangBrukerListForPensjonsakerByFagsakId(fagsakInput);
			} else {
				return findTilgangBrukerListForGsaksakerByFagsakIdAndFagsaksystem(fagsakInput);
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerList feilet ved oppslag. fagsakInput={}", fagsakInput, e);
		}
		return new ArrayList<>();
	}


	private List<TilgangBruker> findTilgangBrukerListForGsaksakerByFagsakIdAndFagsaksystem(FagsakInput fagsakInput) {
		try {
			Map<String, List<String>> idLists = gsakAntiCorruptionLayer.findIdListsByFagsakIdAndFagsaksystem(fagsakInput.getFagsakId(),
					fagsakInput.getFagsaksystem());
			if (idLists.isEmpty()) {
				return new ArrayList<>();
			}

			List<TilgangBruker> tilgangBrukerPerson = aktoerAntiCorruptionLayer.hentTilgangBrukerListByAktoerIdList(idLists.get(AKTOER_ID_LIST));
			Stream<TilgangBruker> tilgangbrukerOrganisasjon = idLists.get(ORGNR_LIST).stream()
					.map(orgnr -> TilgangBruker.builder().orgnummer(orgnr).build());

			return Stream.concat(tilgangBrukerPerson.stream(), tilgangbrukerOrganisasjon).toList();
		} catch (Exception e) {
			log.warn("findTilgangBrukerListForGsaksakerByFagsakIdAndFagsaksystem feilet ved oppslag. fagsakInput={}", fagsakInput, e);
			return new ArrayList<>();
		}
	}

	private List<TilgangBruker> findTilgangBrukerListForPensjonsakerByFagsakId(FagsakInput fagsakInput) {
		try {
			String fnr = pensjonSakAntiCorruptionLayer.findFoedselsnummerBySakId(fagsakInput.getFagsakId());
			return Collections.singletonList(aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(fnr));
		} catch (Exception e) {
			log.warn("findTilgangBrukerListForPensjonsakerByFagsakId feilet ved oppslag. fagsakInput={}", fagsakInput, e);
			return new ArrayList<>();
		}
	}

	public List<TilgangSak> findTilgangSaker(final List<TilgangBruker> tilgangBrukerList, final FagsakInput fagsakInput, final List<Tema> tema, final SafRequestContext safRequestContext) {
		if (fagsakInput.getFagsaksystem().equals(PSAK_FAGSYSTEM)) {
			return findTilgangSakForPsaker(tilgangBrukerList, fagsakInput, tema, safRequestContext);
		} else {
			return findTilgangSakForGsaker(tilgangBrukerList, fagsakInput, tema, safRequestContext);

		}
	}

	private List<TilgangSak> findTilgangSakForGsaker(List<TilgangBruker> filteredTilgangBrukerList, FagsakInput fagsakInput, List<Tema> tema, SafRequestContext safRequestContext) {
		try {
			// For å unngå å gjøre ett kall for hver bruker hentes alle saker assosiert med det aktuelle fagsaknummeret og fagsaksystemet fra Gsak i én spørring
			List<Arkivsak> arkivsaker = gsakAntiCorruptionLayer.findTilgangSakListByFagsakIdAndFagsaksystem(fagsakInput.getFagsakId(), fagsakInput
					.getFagsaksystem(), tema);

			List<String> aktoerIdList = extractAktoerIdListFromFilteredTilgangBrukerList(filteredTilgangBrukerList);
			List<String> orgnrList = extractOrgnrListFromFilteredTilgangBrukerList(filteredTilgangBrukerList);

			return arkivsaker.stream()
					// Vi er kun interesserte i saker tilhørende brukere som ikke ble filtrert bort i pep1
					.filter(arkivsak -> aktoerIdList.contains(arkivsak.getAktoerId()) || orgnrList.contains(arkivsak.getOrgnummer()))
					.map(arkivsak -> {
						safRequestContext.getRequestCache().putArkivsak(arkivsak);
						final BidragSak bidragSak = bisysAntiCorruptionLayer.hentBidragSakByArkivsak(arkivsak);
						List<String> fpsak = fpsakAntiCorruptionLayer.hentRelevanteParter(arkivsak);
						List<String> k9sak = k9AntiCorruptionLayer.hentRelevanteParter(arkivsak);
						return TilgangSak.builder()
								.aktoerId(arkivsak.getAktoerId())
								.orgnummer(arkivsak.getOrgnummer())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem())
								.fagsaksystem(arkivsak.getFagsaksystem())
								.tema(arkivsak.getTema())
								.avsluttet(arkivsak.isAvsluttet())
								.fpAktoerIdList(fpsak)
								.k9AktoerIdList(k9sak)
								.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
								.build();
					}).toList();
		} catch (Exception e) {
			log.warn("findTilgangSakForGsaker feilet ved for fagsakInput={}.", fagsakInput, e);
		}
		return new ArrayList<>();
	}

	private List<String> extractAktoerIdListFromFilteredTilgangBrukerList(List<TilgangBruker> filteredTilgangBrukerList) {
		return filteredTilgangBrukerList.stream()
				.filter(TilgangBruker::isPerson)
				.flatMap(t -> t.getAlleAktoerIds().stream())
				.collect(Collectors.toList());
	}

	private List<String> extractOrgnrListFromFilteredTilgangBrukerList(List<TilgangBruker> filteredTilgangBrukerList) {
		return filteredTilgangBrukerList.stream()
				.filter(tilgangBruker -> !tilgangBruker.isPerson())
				.map(TilgangBruker::getOrgnummer)
				.collect(Collectors.toList());
	}

	private List<TilgangSak> findTilgangSakForPsaker(List<TilgangBruker> tilgangBrukerList, FagsakInput fagsakInput, List<Tema> tema, SafRequestContext safRequestContext) {
		try {
			if (tilgangBrukerList.size() != 1) {
				log.warn("findTilgangSakForPsaker ble kalt med null eller mer enn én bruker. Pensjonssaker kan kun ha én bruker.");
				return new ArrayList<>();
			}

			List<Arkivsak> arkivsaker = pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBrukerList.get(0), tema);
			return arkivsaker.stream()
					.map(arkivsak -> {
						safRequestContext.getRequestCache().putArkivsak(arkivsak);
						return TilgangSak.builder()
								.aktoerId(arkivsak.getAktoerId())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem())
								.avsluttet(arkivsak.isAvsluttet())
								.tema(arkivsak.getTema())
								.relevanteTredjeparter(new ArrayList<>())
								.build();
					}).collect(Collectors.toList());
		} catch (Exception e) {
			log.warn("findTilgangSakForPsaker feilet ved for fagsakInput={}.", fagsakInput, e);
		}
		return new ArrayList<>();
	}
}
