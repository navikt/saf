package no.nav.saf.query.tilknyttedejournalposter;

import no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper;
import no.nav.saf.anticorruptionlayer.joark.safintern.DokarkivTilknyttetJournalpostConsumer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
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
	private final DokarkivTilknyttetJournalpostConsumer dokarkivTilknyttetJournalpostConsumer;
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;
	private final Pep<TilgangSak> pep7d;

	public TilknyttedeJournalposterQuery(
			TilknyttedeJournalposterTilgangRepository tilknyttedeJournalposterTilgangRepository,
			DokarkivTilknyttetJournalpostConsumer dokarkivTilknyttetJournalpostConsumer,
			Pep<TilgangBruker> pep1g,
			Pep<TilgangSak> pep2,
			Pep<TilgangSak> pep2d,
			Pep<TilgangSak> pep3,
			Pep<TilgangJournalpost> pep4,
			Pep<TilgangDokumentInfo> pep5,
			Pep<TilgangDokumentvariant> pep6d,
			Pep<TilgangSak> pep7d) {
		this.tilknyttedeJournalposterTilgangRepository = tilknyttedeJournalposterTilgangRepository;
		this.dokarkivTilknyttetJournalpostConsumer = dokarkivTilknyttetJournalpostConsumer;
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
		List<ArkivJournalpost> datagrunnlag = dokarkivTilknyttetJournalpostConsumer.hentTilknyttedeJournalposter(dokumentInfoId);
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

		return mapArkivJournalpost(filteredTilgangJournalposter, safRequestContext);
	}

	private List<Journalpost> mapArkivJournalpost(final List<TilgangJournalpost> tilgangJournalposts, final SafRequestContext safRequestContext) {
		return tilgangJournalposts.stream()
				.map(tj -> {
					ArkivJournalpost arkivJournalpost = safRequestContext.getRequestCache().getArkivJournalpost(tj.getJournalpostId());
					return ArkivJournalpostMapper.mapJournalpost(arkivJournalpost, safRequestContext.getRequestCache());
				}).collect(Collectors.toList());
	}
}
