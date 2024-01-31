package no.nav.saf.tilgangskontroll.abac.dto.response;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

public final class AdviceStringUtil {
	private static final String ADVICE_SEPERATOR = ".";

	private AdviceStringUtil() {
		//noop
	}

	@NotNull
	public static Map<String,String> getAdvicesMap(List<Advice> advices) {
		if (advices == null) {
			return Collections.emptyMap();
		}
		return advices.stream()
				.flatMap(a -> a.getAttributeAssignments().stream())
				.collect(Collectors.toMap(as -> substringAfterLast(as.getAttributeId(), ADVICE_SEPERATOR), AttributeAssignment::getValue));
	}
}
