package no.nav.saf.tilgangskontroll.abac.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.nav.saf.tilgangskontroll.abac.service.advice.AdviceTypes.DENY_INFO;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class XacmlResponse {
    private static final XacmlResponse PERMIT = new XacmlResponse(Decision.PERMIT, Decision.PERMIT, Collections.emptyList(), Collections.emptyList());
    private static final XacmlResponse DENY = new XacmlResponse(Decision.DENY, Decision.DENY, Collections.emptyList(), Collections.emptyList());
    private static final String ADVICE_SEPERATOR = ".";

    private final Decision decision;
    private final Decision originalDecision;
    private final List<Obligation> obligations;
    private final List<Advice> advices;

    public Map<String, String> getAdvicesMap() {
        if (advices == null) {
            return Collections.emptyMap();
        }
        return advices.stream()
            .flatMap(a -> a.getAttributeAssignments().stream())
            .collect(Collectors.toMap(as -> substringAfterLast(as.getAttributeId(), ADVICE_SEPERATOR), AttributeAssignment::getValue));
    }

    public boolean isPermit() {
        return Decision.PERMIT.equals(decision);
    }

    public boolean isDeny() {
        return Decision.DENY.equals(decision);
    }

    public static XacmlResponse permit() {
        return PERMIT;
    }

    public static XacmlResponse deny() {
        return DENY;
    }

    public static XacmlResponse denyWithInfo(final String info) {
        final Advice adviceInfo = Advice.builder()
				.id(DENY_INFO)
				.attributeAssignments(Collections.singletonList(AttributeAssignment.builder()
						.attributeId("no.nav.saf_info")
						.value(info)
						.build()))
				.build();
        return new XacmlResponse(Decision.DENY, Decision.DENY, Collections.emptyList(), Collections.singletonList(adviceInfo));
    }
}