package no.nav.saf.anticorruptionlayer.aktoer;

import no.nav.saf.anticorruptionlayer.pdl.IdentConsumer;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class PdlAntiCorruptionLayerImpl implements PdlAntiCorruptionLayer {

	private final IdentConsumer identConsumer;

	public PdlAntiCorruptionLayerImpl(IdentConsumer identConsumer) {
		this.identConsumer = identConsumer;
	}

	@Override
	public TilgangBruker hentTilgangBrukerByAktoerId(String aktoerId) {
		if(isBlank(aktoerId)) {
			return TilgangBruker.builder()
					.build();
		}

		return TilgangsbrukerMapper.map(identConsumer.hentIdenter(aktoerId), aktoerId, null);
	}

	@Override
	public TilgangBruker hentTilgangBrukerByFoedselsnummer(String foedselsnummer) {
		if(isBlank(foedselsnummer)) {
			return TilgangBruker.builder()
					.build();
		}
		return TilgangsbrukerMapper.map(identConsumer.hentIdenter(foedselsnummer), null, foedselsnummer);
	}

	@Override
	public List<TilgangBruker> hentTilgangBrukerListByAktoerIdList(List<String> aktoerIdList) {
		if (aktoerIdList.isEmpty()) {
			return new ArrayList<>();
		}

		List<TilgangBruker> tilgangBrukerList = new ArrayList<>();
		for(String aktoerId : aktoerIdList) {
			tilgangBrukerList.add(TilgangsbrukerMapper.map(identConsumer.hentIdenter(aktoerId), aktoerId, null));
		}
		return tilgangBrukerList;
	}


}