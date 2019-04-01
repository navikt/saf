package no.nav.saf.domain.visningsmodell;

import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class SideInfo {
	private static final SideInfo EMPTY_SIDE_INFO = new SideInfo(null, false);

	private final String sluttpeker;
	private final boolean finnesNesteSide;

	public static SideInfo empty() {
		return EMPTY_SIDE_INFO;
	}
}
