package no.nav.saf.anticorruptionlayer.aktoer;

import no.nav.saf.anticorruptionlayer.aktoer.aktoerv2.AktoerV2Consumer;
import no.nav.saf.anticorruptionlayer.aktoer.domain.HentAktoerIdForIdentResponseTo;
import no.nav.saf.anticorruptionlayer.aktoer.domain.HentIdentForAktoerIdListeResponseTo;
import no.nav.saf.anticorruptionlayer.aktoer.domain.HentIdentForAktoerIdResponseTo;
import no.nav.saf.anticorruptionlayer.pdl.IdentConsumer;
import no.nav.saf.anticorruptionlayer.pdl.PdlResponse;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class AktoerAntiCorruptionLayerImpl implements AktoerAntiCorruptionLayer {

	private final IdentConsumer identConsumer;

	public AktoerAntiCorruptionLayerImpl(IdentConsumer identConsumer) {
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