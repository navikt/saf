package no.nav.saf.tilgangskontroll.abac.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AttributeAssignment {
    String attributeId;
    String value;
    String category;
    String dataType;
}