package no.nav.saf.tilgangskontroll.abac.dto.response;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class AdviceStringUtil {
	private static final String ADVICE_SEPERATOR = ".";
	private static final String BLANK = "";

	private AdviceStringUtil() {
		//noop
	}

	public static String convertToString(List<Advice> advices) {
		if (advices == null) {
			return BLANK;
		}
		return advices.isEmpty() ? BLANK :
				(advices.stream()
						.flatMap(a -> a.getAttributeAssignments().stream())
						.map(as -> substringAfterLast(as.getAttributeId(), ADVICE_SEPERATOR) + "=" + as.getValue())
						.collect(Collectors.joining(", ")));
	}
}
