package no.nav.saf.tilgangskontroll;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSaksrelasjon;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;

public class RequestCache {
	private final Map<String, Object> holder = new ConcurrentHashMap<>(300);

	public void putDecision(String key, AbacAnswer abacAnswer) {
		holder.put(key, abacAnswer);
	}

	public void putArkivJournalpost(ArkivJournalpost object) {
		holder.put(object.journalpostId().toString(), object);
	}

	public void putJournalpost(String key, JournalpostDto object) {
		holder.put(key, object);
	}

	public void putArkivsak(Arkivsak arkivsak) {
		holder.put(arkivsak.getKey(), arkivsak);
	}

	public void putTilgangBruker(TilgangBruker tilgangBruker) {
		holder.put(TILGANG_BRUKER, tilgangBruker);
	}

	public AbacAnswer getCachedDecision(String key) {
		return (AbacAnswer) holder.get(key);
	}

	public JournalpostDto getJournalpost(String key) {
		return (JournalpostDto) holder.get(key);
	}

	public ArkivJournalpost getArkivJournalpost(String key) {
		return (ArkivJournalpost) holder.get(key);
	}

	public TilgangBruker getTilgangBruker() {
		return (TilgangBruker) holder.get(TILGANG_BRUKER);
	}

	public Arkivsak getArkivsak(TilgangSak sak) {
		return (Arkivsak) holder.get(sak.getArkivsaksnummer() + sak.getArkivsaksystem());
	}

	public Arkivsak getArkivsak(SaksrelasjonDto saksrelasjon) {
		return (Arkivsak) holder.get(saksrelasjon.getSakId() + FagsystemCode.toSafArkivsaksystem(saksrelasjon.getFagsystem()));
	}

	public Arkivsak getArkivsak(ArkivSaksrelasjon saksrelasjon) {
		return (Arkivsak) holder.get(saksrelasjon.getKey());
	}
}
