package no.nav.saf.tilgangskontroll;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class RequestCache {
	private final Map<String, Object> requestCache = new HashMap<>(300);

	public void putObjects(Map<String, ?> objectMap) {
		requestCache.putAll(objectMap);
	}

	public void putObject(String key, Object object) {
		requestCache.put(key, object);
	}

	@SuppressWarnings("unchecked")
	public <T> T getObject(String key) {
		return (T) requestCache.get(key);
	}
}
