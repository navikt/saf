package no.nav.saf.context.gsak.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@JsonDeserialize(builder = GsakSakerTo.GsakSakerToBuilder.class)
@Value
@Builder
public class GsakSakerTo {
	private final Integer id;
	private final String tema;
	private final String applikasjon;
	private final String aktoerId;
	private final String orgnr;
	private final String fagsakNr;
	private final String opprettetAv;
	private final OffsetDateTime opprettetTidspunkt;

	@JsonPOJOBuilder(withPrefix = "")
	public static class GsakSakerToBuilder {

	}
}

