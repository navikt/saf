package no.nav.saf.tilgangskontroll;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class RequestCache {
	private final Map<String, Object> holder = new ConcurrentHashMap<>(300);

	public void putObjects(Map<String, ?> objectMap) {
		holder.putAll(objectMap);
	}

	public void putObject(String key, Object object) {
		holder.put(key, object);
	}

	@SuppressWarnings("unchecked")
	public <T> T getObject(String key) {
		return (T) holder.get(key);
	}
}
