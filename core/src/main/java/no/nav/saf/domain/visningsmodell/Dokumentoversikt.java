package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Dokumentoversikt {
	@Builder.Default
	private final List<Journalpost> journalposter = new ArrayList<>();
	private final SideInfo sideInfo;

	public static Dokumentoversikt empty() {
		return new Dokumentoversikt(new ArrayList<>(), SideInfo.empty());
	}

}
