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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static no.nav.saf.cache.LokalCacheConfig.MSGRAPH_MEMBER_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.MSGRAPH_USER_CACHE;
import static no.nav.saf.tilgangskontroll.SafSecurityContext.NAVIDENT_PATTERN;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Slf4j
public class MsGraphConsumer {
	private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$");
	private final GraphServiceClient graphClient;

	public MsGraphConsumer(SafProperties safProperties,
						   AzureProperties azureProperties) {
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.tenantId(azureProperties.appTenantId())
				.clientId(azureProperties.appClientId())
				.clientSecret(azureProperties.appClientSecret())
				.build();
		this.graphClient = new GraphServiceClient(clientSecretCredential);
		String overrideMsGraphBaseUrl = safProperties.getEndpoints().getOverrideMsGraphServiceRoot();
		if (isNotBlank(overrideMsGraphBaseUrl)) {
			this.graphClient.getRequestAdapter().setBaseUrl(overrideMsGraphBaseUrl);
		}
	}

	@Cacheable(MSGRAPH_USER_CACHE)
	public Optional<User> getUser(String navIdent) {
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

	@Cacheable(value = MSGRAPH_MEMBER_CACHE, key = "#user.id")
	public boolean isMemberOf(User user, String adGroup) {
		if (user == null || user.getId() == null) {
			return false;
		}
		if (!UUID_PATTERN.matcher(adGroup).matches()) {
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
}
