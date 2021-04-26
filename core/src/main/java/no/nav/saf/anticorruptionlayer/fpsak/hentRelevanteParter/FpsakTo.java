package no.nav.saf.anticorruptionlayer.fpsak.hentRelevanteParter;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class FpsakTo {
	private final List<String> aktoerIds;
	private final String feilmelding;
}
