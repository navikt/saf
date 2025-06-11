package no.nav.saf.anticorruptionlayer.gsak.hentgsaksaker;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@JsonDeserialize(builder = GsakSakerTo.GsakSakerToBuilder.class)
@Value
@Builder
public class GsakSakerTo {
	Integer id;
	String tema;
	String applikasjon;
	String aktoerId;
	String orgnr;
	String fagsakNr;
	String opprettetAv;
	String sakStatus;
	OffsetDateTime opprettetTidspunkt;

	@JsonPOJOBuilder(withPrefix = "")
	public static class GsakSakerToBuilder {

	}
}
