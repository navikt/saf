package no.nav.saf.tjeneste.visningsmodell;

import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class SideInfo {
	private static final SideInfo EMPTY_SIDE_INFO = new SideInfo(null, false, null, false);

	private final String sluttpeker;
	private final boolean finnesNesteSide;
	private final String startpeker;
	private final boolean finnesForrigeSide;

	public static SideInfo empty() {
		return EMPTY_SIDE_INFO;
	}
}
