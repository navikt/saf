package no.nav.saf.query.tilknyttedejournalposter;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.saf.util.MDCUtility.addMdcData;

@Component
public class TilknyttedeJournalposterQuery {
	private final TilknyttedeJournalposterTilgangRepository tilknyttedeJournalposterTilgangRepository;
	private final JournalpostDtoMapper journalpostDtoMapper;
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;
	private final Pep<TilgangSak> pep7d;

	@Autowired
	public TilknyttedeJournalposterQuery(
			TilknyttedeJournalposterTilgangRepository tilknyttedeJournalposterTilgangRepository,
			JournalpostDtoMapper journalpostDtoMapper,
			@Autowired Pep<TilgangBruker> pep1g,
			@Autowired Pep<TilgangSak> pep2,
			@Autowired Pep<TilgangSak> pep2d,
			@Autowired Pep<TilgangSak> pep3,
			@Autowired Pep<TilgangJournalpost> pep4,
			@Autowired Pep<TilgangDokumentInfo> pep5,
			@Autowired Pep<TilgangDokumentvariant> pep6d,
			@Autowired Pep<TilgangSak> pep7d) {
		this.tilknyttedeJournalposterTilgangRepository = tilknyttedeJournalposterTilgangRepository;
		this.journalpostDtoMapper = journalpostDtoMapper;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
		this.pep7d = pep7d;
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
					if (tilgangBruker.isPerson()) {
						return tilgangBruker.getAktoerId().equals(tilgangSak.getAktoerId());
					} else {
						return tilgangBruker.getOrgnummer().equals(tilgangSak.getOrgnummer());
					}
				}))
				.filter(tilgangSak -> pep2.hasAccess(tilgangSak, safRequestContext))
				.peek(tilgangSak -> pep2d.hasAccess(tilgangSak, safRequestContext))
				.filter(tilgangSak -> pep3.hasAccess(tilgangSak, safRequestContext))
				.peek(tilgangSak -> pep7d.hasAccess(tilgangSak, safRequestContext))
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
					JournalpostDto journalpostDto = safRequestContext.getRequestCache().getJournalpost(tj.getJournalpostId());
					return journalpostDtoMapper.mapJournalpostDto(journalpostDto, safRequestContext.getRequestCache());
				}).collect(Collectors.toList());
	}
}
