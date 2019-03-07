package no.nav.saf.query.tilknyttedejournalposter;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903.TilknytningUriParam;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903.TilknyttetJournalpostDto;
import no.nav.saf.domain.kode.Tilknytning;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class TilknyttedeJournalposterTilgangRepository {
	private final HentJournalsakinfo hentJournalsakinfo;

	@Inject
	TilknyttedeJournalposterTilgangRepository(HentJournalsakinfo hentJournalsakinfo) {
		this.hentJournalsakinfo = hentJournalsakinfo;
	}

	List<TilknyttetJournalpostDto> tilgangsmodell(final String dokumentInfoId, Tilknytning tilknytning) {
		return hentJournalsakinfo.tilknyttedeJournalposter(dokumentInfoId, TilknytningUriParam.toUriParam(tilknytning)).getTilknyttedeJournalposter();
	}
}
