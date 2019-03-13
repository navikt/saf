package no.nav.saf.query.tilknyttedejournalposter;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class TilknyttedeJournalposterAntiCorruptionLayer {
	private final HentJournalsakinfo hentJournalsakinfo;

	@Inject
	TilknyttedeJournalposterAntiCorruptionLayer(HentJournalsakinfo hentJournalsakinfo) {
		this.hentJournalsakinfo = hentJournalsakinfo;
	}
}
