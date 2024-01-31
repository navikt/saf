package no.nav.saf.tilgangskontroll;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
