package no.nav.saf.tjeneste.visningsmodell;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
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
		return new Dokumentoversikt(Collections.emptyList(), SideInfo.empty());
	}
}
