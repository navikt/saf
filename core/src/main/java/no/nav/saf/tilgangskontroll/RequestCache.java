package no.nav.saf.tilgangskontroll;

import lombok.Getter;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSaksrelasjon;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.pep.PepAnswer;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;

public class RequestCache {
	@Getter
	private final boolean isSystem;

	private final Map<String, Object> holder = new ConcurrentHashMap<>(300);

	public RequestCache(boolean isSystem) {
		this.isSystem = isSystem;
	}

	public void putDecision(String key, PepAnswer pepAnswer) {
		holder.put(key, pepAnswer);
	}

	public void putArkivsak(Arkivsak arkivsak) {
		holder.put(arkivsak.getKey(), arkivsak);
	}

	public void putTilgangBruker(TilgangBruker tilgangBruker) {
		holder.put(TILGANG_BRUKER, tilgangBruker);
	}

	public PepAnswer getCachedDecision(String key) {
		return (PepAnswer) holder.get(key);
	}

	public Optional<TilgangBruker> getTilgangBruker() {
		return Optional.ofNullable((TilgangBruker) holder.get(TILGANG_BRUKER));
	}

	public Arkivsak getArkivsak(SaksrelasjonDto saksrelasjon) {
		return (Arkivsak) holder.get(saksrelasjon.getSakId() + FagsystemCode.toSafArkivsaksystem(saksrelasjon.getFagsystem()));
	}

	public Arkivsak getArkivsak(ArkivSaksrelasjon saksrelasjon) {
		return (Arkivsak) holder.get(saksrelasjon.getKey());
	}
}
