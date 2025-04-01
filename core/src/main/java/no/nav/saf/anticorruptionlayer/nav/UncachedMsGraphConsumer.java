package no.nav.saf.anticorruptionlayer.nav;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.ApiException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.azure.AzureProperties;
import no.nav.saf.config.SafProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static no.nav.saf.tilgangskontroll.SafSecurityContext.NAVIDENT_PATTERN;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Slf4j
public class UncachedMsGraphConsumer {
	private final String azureGroupsFilter;
	private final GraphServiceClient graphClient;

	public UncachedMsGraphConsumer(SafProperties safProperties,
								   AzureProperties azureProperties) {
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.tenantId(azureProperties.appTenantId())
				.clientId(azureProperties.appClientId())
				.clientSecret(azureProperties.appClientSecret())
				.build();
		this.graphClient = new GraphServiceClient(clientSecretCredential, "https://graph.microsoft.com/.default");
		this.azureGroupsFilter = createAzureGroupFilter(safProperties.getAzureGroup());
		String overrideMsGraphBaseUrl = safProperties.getEndpoints().getOverrideMsGraphServiceRoot();
		if (isNotBlank(overrideMsGraphBaseUrl)) {
			this.graphClient.getRequestAdapter().setBaseUrl(overrideMsGraphBaseUrl);
		}
	}

	Optional<User> getUser(String navIdent) {
		if (!NAVIDENT_PATTERN.matcher(navIdent).matches()) {
			return Optional.empty();
		}
		try {
			List<User> res = graphClient
					.users()
					.get(requestConfiguration -> {
						requestConfiguration.headers.add("ConsistencyLevel", "eventual");
						requestConfiguration.queryParameters.filter = "onPremisesSamAccountName eq '" + navIdent + "'";
						requestConfiguration.queryParameters.count = true;
						requestConfiguration.queryParameters.select = new String[]{"id"};
					}).getValue();
			if (res.isEmpty()) {
				log.error("Microsoft Graph finner ikke bruker med navIdent={}", navIdent);
				return Optional.empty();
			}
			return Optional.of(res.get(0));
		} catch (ApiException e) {
			log.error("Teknisk feil mot Microsoft Graph. message=" + e.getMessage(), e);
			return Optional.empty();
		}
	}

	boolean isMemberOf(User user, UUID adGroup) {
		if (user == null || user.getId() == null) {
			return false;
		}

		try {
			List<DirectoryObject> res = graphClient
					.users().byUserId(user.getId())
					.memberOf()
					.get(requestConfiguration -> {
						requestConfiguration.headers.add("ConsistencyLevel", "eventual");
						requestConfiguration.queryParameters.filter = "id eq '" + adGroup + "'";
						requestConfiguration.queryParameters.count = true;
						requestConfiguration.queryParameters.select = new String[]{"id"};
					})
					.getValue();
			return !res.isEmpty();
		} catch (ApiException e) {
			log.error("Teknisk feil mot Microsoft Graph. message=" + e.getMessage(), e);
			return false;
		}
	}

	Set<String> getRelevantGroupsForUser(Optional<User> user) {
		if (user.isEmpty() || user.get().getId() == null) {
			return emptySet();
		}

		try {
			List<DirectoryObject> res = graphClient
					.users().byUserId(user.get().getId())
					.memberOf()
					.get(requestConfiguration -> {
						requestConfiguration.headers.add("ConsistencyLevel", "eventual");
						requestConfiguration.queryParameters.filter = azureGroupsFilter;
						requestConfiguration.queryParameters.count = false;
						requestConfiguration.queryParameters.select = new String[]{"id"};
					})
					.getValue();
			return res.stream().map(DirectoryObject::getId).collect(Collectors.toSet());
		} catch (ApiException e) {
			log.error("Teknisk feil mot Microsoft Graph. message=" + e.getMessage(), e);
			return emptySet();
		}
	}

	private static String createAzureGroupFilter(SafProperties.AzureGroup azureGroup) {
		return "(" + azureGroup.getAllGroupUUIDsAsStream()
				.map(UUID::toString)
				.map(str -> "id eq '" + str + "'")
				.collect(Collectors.joining(" or ")) +
				")";
	}
}
