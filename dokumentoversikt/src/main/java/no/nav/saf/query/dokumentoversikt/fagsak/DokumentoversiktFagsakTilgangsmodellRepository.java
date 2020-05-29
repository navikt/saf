package no.nav.saf.query.dokumentoversikt.fagsak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.AktoerAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.cache.LokalCacheConfig;
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
import static no.nav.saf.domain.DomainConstants.AKTOER_ID_LIST;
import static no.nav.saf.domain.DomainConstants.ORGNR_LIST;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class DokumentoversiktFagsakTilgangsmodellRepository {
	private final AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer;
	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;

	public DokumentoversiktFagsakTilgangsmodellRepository(AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer,
														  GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
														  PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
														  BisysAntiCorruptionLayer bisysAntiCorruptionLayer) {
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
	}


	@Cacheable(cacheNames = LokalCacheConfig.TILGANGSMODELL_REPO_BRUKER_CACHE)
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
			Map<String, List<String>> idLists = gsakAntiCorruptionLayer.findIdListsByFagsakIdAndFagsaksystem(fagsakInput.getFagsakId(), fagsakInput
					.getFagsaksystem());
			if (idLists.isEmpty()) {
				return new ArrayList<>();
			}

			List<TilgangBruker> tilgangBrukerPerson = aktoerAntiCorruptionLayer.hentTilgangBrukerListByAktoerIdList(idLists.get(AKTOER_ID_LIST));
			List<TilgangBruker> tilgangbrukerOrganisasjon = idLists.get(ORGNR_LIST).stream()
					.map(orgnr -> TilgangBruker.builder().orgnummer(orgnr).build())
					.collect(Collectors.toList());

			return Stream.concat(tilgangBrukerPerson.stream(), tilgangbrukerOrganisasjon.stream()).collect(Collectors.toList());
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
						safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
						final BidragSak bidragSak = getBidragSakIfTemaIsBidOrFar(arkivsak);
						return TilgangSak.builder()
								.aktoerId(arkivsak.getAktoerId())
								.orgnummer(arkivsak.getOrgnummer())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem())
								.tema(arkivsak.getTema())
								.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
								.paragraf19(bidragSak == null ? null : bidragSak.isParagraf19())
								.build();
					}).collect(Collectors.toList());
		} catch (Exception e) {
			log.warn("findTilgangSakForGsaker feilet ved for fagsakInput={}.", fagsakInput, e);
		}
		return new ArrayList<>();
	}

	private List<String> extractAktoerIdListFromFilteredTilgangBrukerList(List<TilgangBruker> filteredTilgangBrukerList) {
		return filteredTilgangBrukerList.stream()
				.filter(TilgangBruker::isPerson)
				.map(TilgangBruker::getAktoerId)
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
						safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
						return TilgangSak.builder()
								.aktoerId(arkivsak.getAktoerId())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem())
								.tema(arkivsak.getTema())
								.paragraf19(false)
								.relevanteTredjeparter(new ArrayList<>())
								.build();
					}).collect(Collectors.toList());
		} catch (Exception e) {
			log.warn("findTilgangSakForPsaker feilet ved for fagsakInput={}.", fagsakInput, e);
		}
		return new ArrayList<>();
	}

	private BidragSak getBidragSakIfTemaIsBidOrFar(Arkivsak arkivsak) {
		if (Tema.BID.equals(arkivsak.getTema()) || Tema.FAR.equals(arkivsak.getTema())) {
			return bisysAntiCorruptionLayer.hentBidragSak(arkivsak.getFagsakId());
		} else {
			return new BidragSak();
		}
	}
}
