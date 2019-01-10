package no.nav.saf.tilgangskontroll.validation.registry;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdpRegistryImpl implements IdpRegistry {
	private final Map<String, Idp> idpByIssuerMap = new HashMap<>();
	private final Map<String, Idp> idpByNameMap = new HashMap<>();

	public IdpRegistryImpl(Map<String, Idp> idps) {
		idps.forEach(this::add);
		idpByNameMap.putAll(idps);
	}

	private void add(String name, Idp idp) {
		idpByIssuerMap.put(idp.getIssuerUrl(), idp);
		if (idp.getIssuerUrl() == null) {
			throw new IdpException(format("Idp with name: %s does not have issuer url", name));
		}
		if (idp.getJwksUrl() == null) {
			throw new IdpException(format("Idp with name: %s does not have jwks url", name));
		}
	}

	@Override
	public List<Idp> getAll() {
		return new ArrayList<>(idpByIssuerMap.values());
	}
}