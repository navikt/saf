package no.nav.saf.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Data
@ConfigurationProperties("saf")
@Validated
public class SafProperties {

	private final Endpoints endpoints = new Endpoints();

	private final AzureGroup azureGroup = new AzureGroup();

	@NotEmpty
	private String privilegiedserviceusers;

	@Data
	public static class AzureGroup {
		@NotEmpty
		private UUID egenAnsattObjectId;
		// Disse bør annoteres med @NotEmpty etterhvert som de blir tatt i bruk
		private UUID fortroligAdresseObjectId;
		private UUID strengtFortroligAdresseObjectId;
		private UUID joarkVedlikeholdObjectId;
		private UUID pensjonUtvidetObjectId;
		private UUID gosysUtvidetObjectId;

		public Stream<UUID> getAllGroupUUIDsAsStream() {
			return Stream.of(
					egenAnsattObjectId,
					fortroligAdresseObjectId,
					strengtFortroligAdresseObjectId,
					joarkVedlikeholdObjectId,
					pensjonUtvidetObjectId,
					gosysUtvidetObjectId
			).filter(Objects::nonNull);
		}
	}

	@Data
	@Validated
	public static class Endpoints {
		private String overrideMsGraphServiceRoot;

		/**
		 * Baseurl til NAV HR tjenestene
		 */
		@NotEmpty
		private String hrNavUrl;

		/**
		 * Base URL og scope til pensjon
		 */
		@NotNull
		private AzureEndpoint pensjon;

		/**
		 * URL og scope til PDL
		 */
		@NotNull
		private AzureEndpoint pdl;

		/**
		 * URL og scope til safintern oppslagstjenesten i dokarkiv.
		 */
		@NotNull
		private AzureEndpoint dokarkiv;

		/**
		 * Url og scope til fpsak PIP
		 */
		@NotNull
		private AzureEndpoint fpsak;

		/**
		 * Url og scope til k9sak
		 */
		@NotNull
		private AzureEndpoint k9sak;

	}

	@Data
	@Validated
	public static class AzureEndpoint {
		/**
		 * Url til tjeneste som har azure autorisasjon
		 */
		@NotEmpty
		private String url;
		/**
		 * Scope til azure client credential flow
		 */
		@NotEmpty
		private String scope;
	}

}
