package no.nav.saf.tilgangskontroll.abac.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class XacmlResponse {
    private final Decision decision;
    private final Decision originalDecision;
    private final List<Obligation> obligations;
    private final List<Advice> advices;
}