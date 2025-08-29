package no.nav.saf.anticorruptionlayer.sak.hentsaksaker;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@JsonDeserialize(builder = SakSakerTo.SakSakerToBuilder.class)
@Value
@Builder
public class SakSakerTo {
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
	public static class SakSakerToBuilder {

	}
}
