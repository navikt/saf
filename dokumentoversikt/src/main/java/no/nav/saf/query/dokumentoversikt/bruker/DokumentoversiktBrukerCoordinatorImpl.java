package no.nav.saf.query.dokumentoversikt.bruker;

import static no.nav.saf.domain.DomainConstants.PEP1G;
import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.DomainConstants.PEP2D;
import static no.nav.saf.domain.DomainConstants.PEP3;
import static no.nav.saf.domain.DomainConstants.PEP4;
import static no.nav.saf.domain.DomainConstants.PEP5;
import static no.nav.saf.domain.DomainConstants.PEP6D;
import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.query.dokumentoversikt.DokumentoversiktVisningsmodellRepository;
import no.nav.saf.query.dokumentoversikt.SideInfoMapper;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktPagination;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
class DokumentoversiktBrukerCoordinatorImpl implements DokumentoversiktBrukerCoordinator {

	private final SideInfoMapper sideInfoMapper = new SideInfoMapper();
	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final DokumentoversiktVisningsmodellRepository visningsmodellRepository;
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;

	@Inject
	public DokumentoversiktBrukerCoordinatorImpl(TilgangsmodellRepository tilgangsmodellRepository,
												 DokumentoversiktVisningsmodellRepository visningsmodellRepository,
												 @Named(PEP1G) Pep<TilgangBruker> pep1g,
												 @Named(PEP2) Pep<TilgangSak> pep2,
												 @Named(PEP2D) Pep<TilgangSak> pep2d,
												 @Named(PEP3) Pep<TilgangSak> pep3,
												 @Named(PEP4) Pep<TilgangJournalpost> pep4,
												 @Named(PEP5) Pep<TilgangDokumentInfo> pep5,
												 @Named(PEP6D) Pep<TilgangDokumentvariant> pep6d) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.visningsmodellRepository = visningsmodellRepository;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
	}

	@Override
	@Monitor(value = "dok_request", extraTags = {"process", "dokumentOversikt", "requestType", "bruker"}, histogram = true)
	public Dokumentoversikt hentDokumentoversikt(DokumentoversiktBrukerArguments dokumentoversiktBrukerArguments, SafRequestContext safRequestContext) {
		TilgangBruker tilgangBruker = tilgangsmodellRepository.findTilgangBruker(dokumentoversiktBrukerArguments.getBrukerIdInput());
		if (tilgangBruker != null) {
			safRequestContext.getRequestCache().putObject(TILGANG_BRUKER, tilgangBruker);
		}

		boolean pep1gAccess = this.pep1g.hasAccess(tilgangBruker, safRequestContext);

		if (!pep1gAccess) {
			return Dokumentoversikt.empty();
		}

		/**
		 * Resultat fra pep2d cahces lokalt og brukes i JournalpostDtoMapper.java. Med bakgrunn i resultat fra pep2d og pep6d settes feltet saksbehandlerHarTilgang=true/false.
		 **/
		final Flowable<TilgangSak> tilgangSakFlow = tilgangsmodellRepository.findTilgangSaker(tilgangBruker, dokumentoversiktBrukerArguments
				.getFilters()
				.getTema(), safRequestContext);
		List<TilgangSak> filteredTilgangSakList = tilgangSakFlow
				.onErrorResumeNext(throwable -> {
					return Flowable.empty();
				})
				.parallel(10)
				.runOn(Schedulers.io())
				.filter(ts -> pep2.hasAccess(ts, safRequestContext))
				.doOnNext(ts -> pep2d.hasAccess(ts, safRequestContext))
				.filter(ts -> pep3.hasAccess(ts, safRequestContext))
				.sequential()
				.toList().blockingGet();

		List<TilgangJournalpost> tilgangJournalposter = tilgangsmodellRepository.findTilgangJournalposter(
				Collections.singletonList(tilgangBruker),
				filteredTilgangSakList,
				dokumentoversiktBrukerArguments.getFilters().getFraDato(),
				dokumentoversiktBrukerArguments.getFilters().getTema(),
				dokumentoversiktBrukerArguments.getFilters().getJournalposttyper(),
				dokumentoversiktBrukerArguments.getFilters().getJournalstatuser(),
				((DokumentoversiktPagination.SeekPagination) dokumentoversiktBrukerArguments.getPagination()).getFoerste(),
				((DokumentoversiktPagination.SeekPagination) dokumentoversiktBrukerArguments.getPagination()).getEtterPeker(),
				((DokumentoversiktPagination.SeekPagination) dokumentoversiktBrukerArguments.getPagination()).getSiste(),
				((DokumentoversiktPagination.SeekPagination) dokumentoversiktBrukerArguments.getPagination()).getFoerPeker(),
				safRequestContext
		);

		final List<TilgangJournalpost> filteredTilgangJournalpostList = Flowable.fromIterable(tilgangJournalposter)
				.parallel(10)
				.runOn(Schedulers.io())
				.filter(tj -> pep4.hasAccess(tj, safRequestContext))
				.filter(tj -> checkPepIfMidlertidigJournalpost(pep2, tj, tilgangBruker, safRequestContext))
				.filter(tj -> checkPepIfMidlertidigJournalpost(pep2d, tj, tilgangBruker, safRequestContext))
				.sequential()
				.toList()
				.blockingGet();

		/**
		 * Resultat fra pep5 caches lokalt og brukes i JournalpostDtoMapper.java for å filtrere på dokumentinfo-metadata som skal gis til saksbehandler.
		 * Resultat fra pep6d caches også lokalt og brukes i JournalpostDtoMapper.java. Med bakgrunn i resultat fra pep2d og pep6d settes feltet saksbehandlerHarTilgang=true/false.
		 **/
		Flowable.fromIterable(filteredTilgangJournalpostList)
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(tj -> tj.getDokumenter()
						.forEach(tilgangDokumentInfo -> pep5.hasAccess(tilgangDokumentInfo, safRequestContext)))
				.doOnNext(tj -> tj.getDokumenter()
						.forEach(tilgangDokumentInfo -> tilgangDokumentInfo.getTilgangDokumentvarianter()
								.forEach(tilgangDokumentvariant -> pep6d.hasAccess(tilgangDokumentvariant, safRequestContext))));


		List<Journalpost> visningJournalposter = visningsmodellRepository.findJournalposter(filteredTilgangJournalpostList.stream()
				.map(TilgangJournalpost::getJournalpostId)
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList()), safRequestContext);

		return Dokumentoversikt.builder()
				.journalposter(visningJournalposter)
				.sideInfo(sideInfoMapper.mapSideInfo(visningJournalposter, safRequestContext))
				.build();
	}

	private TilgangSak mapToTilgangSak(String journalpostId, TilgangBruker tilgangBruker, SafRequestContext safRequestContext) {
		JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(journalpostId);

		return TilgangSak.builder()
				.foedselsnummer(tilgangBruker.getFoedselsnr())
				.aktoerId(tilgangBruker.getAktoerId())
				.orgnummer(tilgangBruker.getOrgnummer())
				.arkivsaksnummer(journalpostDto.getSaksrelasjon().getSakId())
				.arkivsaksystem(FagsystemCode.toSafArkivsaksystem(journalpostDto.getSaksrelasjon().getFagsystem()))
				.tema(FagomradeCode.toSafTema(journalpostDto.getFagomrade()))
				.build();
	}

	private boolean checkPepIfMidlertidigJournalpost(Pep pep, TilgangJournalpost tj, TilgangBruker tb, SafRequestContext safRequestContext) {
		if (tj.getJournalstatus().equals(Journalstatus.MOTTATT)) {
			return pep.hasAccess(mapToTilgangSak(tj.getJournalpostId(), tb, safRequestContext), safRequestContext);
		} else {
			return true;
		}
	}
}
