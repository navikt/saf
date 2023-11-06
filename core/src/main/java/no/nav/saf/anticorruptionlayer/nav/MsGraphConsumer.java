package no.nav.saf.anticorruptionlayer.nav;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.azure.AzureProperties;
import no.nav.saf.config.SafProperties;
import okhttp3.Request;
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
	private final GraphServiceClient<Request> graphClient;

	public MsGraphConsumer(SafProperties safProperties,
						   AzureProperties azureProperties) {
		this.graphClient = GraphServiceClient.builder()
				.authenticationProvider(new TokenCredentialAuthProvider(new ClientSecretCredentialBuilder()
						.tenantId(azureProperties.appTenantId())
						.clientId(azureProperties.appClientId())
						.clientSecret(azureProperties.appClientSecret())
						.build())).buildClient();
		String overrideMsGraphServiceRoot = safProperties.getEndpoints().getOverrideMsGraphServiceRoot();
		if (isNotBlank(overrideMsGraphServiceRoot)) {
			this.graphClient.setServiceRoot(overrideMsGraphServiceRoot);
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
					.buildRequest(List.of(
							new HeaderOption("ConsistencyLevel", "eventual"),
							new QueryOption("$filter", "onPremisesSamAccountName eq '" + navIdent + "'")
					))
					.count()
					.select("id")
					.get().getCurrentPage();
			if (res.isEmpty()) {
				log.error("Microsoft Graph finner ikke bruker med navIdent={}", navIdent);
				return Optional.empty();
			}
			return Optional.of(res.get(0));
		} catch (ClientException e) {
			log.error("Teknisk feil mot Microsoft Graph. message=" + e.getMessage(), e);
			return Optional.empty();
		}
	}

	@Cacheable(value = MSGRAPH_MEMBER_CACHE, key = "#user.id")
	public boolean isMemberOf(User user, String adGroup) {
		if (user == null || user.id == null) {
			return false;
		}
		if (!UUID_PATTERN.matcher(adGroup).matches()) {
			return false;
		}

		try {
			List<DirectoryObject> res = graphClient
					.users(user.id)
					.memberOf()
					.buildRequest(List.of(
							new HeaderOption("ConsistencyLevel", "eventual"),
							new QueryOption("$filter", "id eq '" + adGroup + "'")
					))
					.count()
					.select("id")
					.get().getCurrentPage();
			return res.size() == 1;
		} catch (ClientException e) {
			log.error("Teknisk feil mot Microsoft Graph. message=" + e.getMessage(), e);
			return false;
		}
	}
}
