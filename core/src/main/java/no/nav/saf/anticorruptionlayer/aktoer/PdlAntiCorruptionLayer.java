package no.nav.saf.anticorruptionlayer.aktoer;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.pdl.IdentConsumer;
import no.nav.saf.anticorruptionlayer.pdl.PersonIkkeFunnetException;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static no.nav.saf.cache.LokalCacheConfig.TILGANGSMODELL_REPO_BRUKER_CACHE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class PdlAntiCorruptionLayer {

	private final IdentConsumer identConsumer;

	public PdlAntiCorruptionLayer(IdentConsumer identConsumer) {
		this.identConsumer = identConsumer;
	}

	@Cacheable(cacheNames = TILGANGSMODELL_REPO_BRUKER_CACHE)
	public TilgangBruker findTilgangBruker(BrukerIdInput brukerIdInput) {
		try {
			return switch (brukerIdInput.getType()) {
				case AKTOERID -> hentTilgangBrukerByAktoerId(brukerIdInput.getId());
				case FNR -> hentTilgangBrukerByFoedselsnummer(brukerIdInput.getId());
				case ORGNR -> TilgangBruker.builder()
						.orgnummer(brukerIdInput.getId())
						.build();
				case null -> null;
			};
		} catch (PersonIkkeFunnetException e) {
			log.info("Fant ikke person i Persondataløsningen (PDL).");
			return null;
		}
	}

	public TilgangBruker hentTilgangBrukerByAktoerId(String aktoerId) {
		if (isBlank(aktoerId)) {
			return TilgangBruker.builder()
					.build();
		}

		return TilgangsbrukerMapper.map(identConsumer.hentIdenter(aktoerId));
	}

	public TilgangBruker hentTilgangBrukerByFoedselsnummer(String foedselsnummer) {
		if (isBlank(foedselsnummer)) {
			return TilgangBruker.builder()
					.build();
		}
		return TilgangsbrukerMapper.map(identConsumer.hentIdenter(foedselsnummer));
	}

	public List<TilgangBruker> hentTilgangBrukerListByAktoerIdList(List<String> aktoerIdList) {
		if (aktoerIdList.isEmpty()) {
			return new ArrayList<>();
		}

		List<TilgangBruker> tilgangBrukerList = new ArrayList<>();
		for (String aktoerId : aktoerIdList) {
			tilgangBrukerList.add(TilgangsbrukerMapper.map(identConsumer.hentIdenter(aktoerId)));
		}
		return tilgangBrukerList;
	}
}