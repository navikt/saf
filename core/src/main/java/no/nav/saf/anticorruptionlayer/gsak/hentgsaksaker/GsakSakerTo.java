package no.nav.saf.anticorruptionlayer.gsak.hentgsaksaker;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

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
	private final ZonedDateTime opprettetTidspunkt;

	@JsonPOJOBuilder(withPrefix = "")
	public static class GsakSakerToBuilder {

	}
}
