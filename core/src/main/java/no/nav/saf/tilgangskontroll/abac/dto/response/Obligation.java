package no.nav.saf.tilgangskontroll.abac.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor
public class Obligation {
    String id;
    List<AttributeAssignment> attributeAssignments;
}