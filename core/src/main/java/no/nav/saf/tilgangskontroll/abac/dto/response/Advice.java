package no.nav.saf.tilgangskontroll.abac.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor
public class Advice {
    private String id;
    private List<AttributeAssignment> attributeAssignments;
}