package no.nav.saf.query.tilknyttedejournalposter;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper;
import no.nav.saf.anticorruptionlayer.joark.safintern.DokarkivTilknyttetJournalpostConsumer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import no.nav.safselvbetjening.tilgang.Ident;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static no.nav.saf.util.MDCUtility.addMdcData;

@Slf4j
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
	private final Pep<TilgangSak> pep8d;

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
			Pep<TilgangSak> pep7d,
			Pep<TilgangSak> pep8d) {
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
		this.pep8d = pep8d;
	}

	public List<Journalpost> hentTilknyttedeJournalposter(String dokumentInfoId, SafRequestContext safRequestContext) {
		addMdcData(safRequestContext);
		List<ArkivJournalpost> journalposter = dokarkivTilknyttetJournalpostConsumer.hentTilknyttedeJournalposter(dokumentInfoId);
		Map<Long, ArkivJournalpost> arkivJournalposter = journalposter.stream()
				.collect(Collectors.toMap(ArkivJournalpost::journalpostId, arkivJournalpost -> arkivJournalpost));

		Map<Long, Arkivsak> arkivsaker = tilknyttedeJournalposterTilgangRepository.arkivsaker(journalposter);

		Map<Long, TilgangBruker> filteredTilgangBruker = tilknyttedeJournalposterTilgangRepository.getTilgangBrukerMap(arkivsaker, journalposter)
				.filter(tilgangBruker -> pep1g.hasAccess(tilgangBruker.getValue(), safRequestContext))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		Set<TilgangSak> filteredTilgangSaker = tilknyttedeJournalposterTilgangRepository.tilgangSaker(arkivsaker.values(), safRequestContext)
				.stream()
				.filter(tilgangSak -> filteredTilgangBruker.values().stream()
						.filter(Objects::nonNull)
						.filter(tilgangBruker -> !tilgangBruker.isUkjent())
						.anyMatch(tilgangBruker -> {
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
				.peek(tilgangSak -> pep8d.hasAccess(tilgangSak, safRequestContext))
				.collect(Collectors.toSet());

		return tilknyttedeJournalposterTilgangRepository.tilgangJournalposter(filteredTilgangSaker, journalposter)
				.stream()
				.filter(tj1 -> pep4.hasAccess(tj1, safRequestContext))
				.peek(tj1 -> tj1.getDokumenter().forEach(td -> {
					pep5.hasAccess(td, safRequestContext);
					td.getTilgangDokumentvarianter().forEach(tdv -> pep6d.hasAccess(tdv, safRequestContext));
				}))
				.map(TilgangJournalpost::getJournalpostId)
				.map(arkivJournalposter::get)
				.map(arkivJournalpost ->
				{
					TilgangBruker tilgangBruker = filteredTilgangBruker.get(arkivJournalpost.journalpostId());
					Set<Ident> brukerIdenter = tilgangBruker == null ? emptySet() :
							tilgangBruker.getBrukersIdenterSomTilgangsIdenter().collect(Collectors.toSet());
					return ArkivJournalpostMapper.mapJournalpost(arkivJournalpost,
							brukerIdenter,
							safRequestContext.getRequestCache());
				})
				.toList();
	}

}
