package no.nav.saf.domain.visningsmodell;

import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class SideInfo {
	private static final SideInfo EMPTY_SIDE_INFO = new SideInfo(0,null, false, null, false);

	// vi vil ikke eksponere denne med mindre konsumententene vil ha det
	@Deprecated
	private final int totaltAntall;
	private final String sluttpeker;
	private final boolean finnesNesteSide;
	// det er ikke noe behov for å paginere bakover
	@Deprecated
	private final String startpeker;
	// det er ikke noe behov for å paginere bakover
	@Deprecated
	private final boolean finnesForrigeSide;

	public static SideInfo empty() {
		return EMPTY_SIDE_INFO;
	}
}
