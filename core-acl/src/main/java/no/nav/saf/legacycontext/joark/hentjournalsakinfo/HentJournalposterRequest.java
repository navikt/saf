package no.nav.saf.legacycontext.joark.hentjournalsakinfo;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class HentJournalposterRequest {
	@Builder.Default
	private final List<String> gsakSakIdList = new ArrayList<>();
	@Builder.Default
	private final List<String> psakSakIdList = new ArrayList<>();
}
