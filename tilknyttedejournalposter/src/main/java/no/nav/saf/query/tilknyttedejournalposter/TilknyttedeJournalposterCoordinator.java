package no.nav.saf.query.tilknyttedejournalposter;

import static no.nav.saf.domain.DomainConstants.PEP1G;
import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.DomainConstants.PEP2D;
import static no.nav.saf.domain.DomainConstants.PEP3;
import static no.nav.saf.domain.DomainConstants.PEP4;
import static no.nav.saf.domain.DomainConstants.PEP5;
import static no.nav.saf.domain.DomainConstants.PEP6D;
import static no.nav.saf.util.MDCUtility.addMdcData;

import no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoMapper;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Tilknytning;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class TilknyttedeJournalposterCoordinator {
	private final TilknyttedeJournalposterTilgangRepository tilknyttedeJournalposterTilgangRepository;
	private final JournalpostDtoMapper journalpostDtoMapper;
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;

	@Inject
	public TilknyttedeJournalposterCoordinator(TilknyttedeJournalposterTilgangRepository tilknyttedeJournalposterTilgangRepository,
											   JournalpostDtoMapper journalpostDtoMapper,
											   @Named(PEP1G) Pep<TilgangBruker> pep1g,
											   @Named(PEP2) Pep<TilgangSak> pep2,
											   @Named(PEP2D) Pep<TilgangSak> pep2d,
											   @Named(PEP3) Pep<TilgangSak> pep3,
											   @Named(PEP4) Pep<TilgangJournalpost> pep4,
											   @Named(PEP5) Pep<TilgangDokumentInfo> pep5,
											   @Named(PEP6D) Pep<TilgangDokumentvariant> pep6d) {
		this.tilknyttedeJournalposterTilgangRepository = tilknyttedeJournalposterTilgangRepository;
		this.journalpostDtoMapper = journalpostDtoMapper;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
	}


	public List<Journalpost> hentTilknyttedeJournalposter(String dokumentInfoId, Tilknytning tilknytning, SafRequestContext safRequestContext) {
		addMdcData(safRequestContext);
		List<JournalpostDto> datagrunnlag = tilknyttedeJournalposterTilgangRepository.datagrunnlag(dokumentInfoId, tilknytning);
		Set<Arkivsak> arkivsaker = tilknyttedeJournalposterTilgangRepository.arkivsaker(datagrunnlag, safRequestContext);

		Set<TilgangBruker> filteredTilgangBruker = tilknyttedeJournalposterTilgangRepository.tilgangBrukere(arkivsaker, datagrunnlag)
				.stream()
				.filter(tilgangBruker -> pep1g.hasAccess(tilgangBruker, safRequestContext))
				.collect(Collectors.toSet());

		Set<TilgangSak> filteredTilgangSaker = tilknyttedeJournalposterTilgangRepository.tilgangSaker(arkivsaker, safRequestContext)
				.stream()
				.filter(tilgangSak -> filteredTilgangBruker.stream().anyMatch(tilgangBruker -> {
					if(tilgangBruker.isBrukerPerson()) {
						return tilgangBruker.getAktoerId().equals(tilgangSak.getAktoerId());
					} else {
						return tilgangBruker.getOrgnummer().equals(tilgangSak.getOrgnummer());
					}
				}))
				.filter(tilgangSak -> pep2.hasAccess(tilgangSak, safRequestContext))
				.peek(tilgangSak -> pep2d.hasAccess(tilgangSak, safRequestContext))
				.filter(tilgangSak -> pep3.hasAccess(tilgangSak, safRequestContext))
				.collect(Collectors.toSet());

		List<TilgangJournalpost> filteredTilgangJournalposter = tilknyttedeJournalposterTilgangRepository.tilgangJournalposter(filteredTilgangSaker, datagrunnlag)
				.stream()
				.filter(tj -> pep4.hasAccess(tj, safRequestContext))
				.peek(tj -> tj.getDokumenter().forEach(td -> {
					pep5.hasAccess(td, safRequestContext);
					td.getTilgangDokumentvarianter().forEach(tdv -> pep6d.hasAccess(tdv, safRequestContext));
				}))
				.collect(Collectors.toList());

		return mapJournalpostDto(filteredTilgangJournalposter, safRequestContext);
	}

	private List<Journalpost> mapJournalpostDto(final List<TilgangJournalpost> tilgangJournalposts, final SafRequestContext safRequestContext) {
		return tilgangJournalposts.stream()
				.map(tj -> {
					JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(tj.getJournalpostId());
					return journalpostDtoMapper.mapJournalpostDto(journalpostDto, safRequestContext.getRequestCache());
				}).collect(Collectors.toList());
	}
}
