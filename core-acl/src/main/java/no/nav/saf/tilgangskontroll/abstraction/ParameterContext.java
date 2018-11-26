package no.nav.saf.tilgangskontroll.abstraction;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains a thread-safe map of search parameters.
 */
public class ParameterContext {
    private final ConcurrentHashMap<String, Object> parameterMap;

    public ParameterContext() {
        parameterMap = new ConcurrentHashMap<>();
    }

    public ParameterContext(Map<String, ? extends Object> parameterMap) {
        this.parameterMap = new ConcurrentHashMap<>(parameterMap);
    }

    public Map<String, Object> getParameters() {
        return parameterMap;
    }

    public boolean containsParameter(String name) {
        return parameterMap.containsKey(name);
    }

    public <T> T getParameter(String name) {
        return (T) parameterMap.get(name);
    }

    public void putParameters(Map<String, ? extends Object> parentSearchParameters) {
        parameterMap.putAll(parentSearchParameters);
    }

    public void putParameter(String parameterName, Object parameterValue) {
        parameterMap.put(parameterName, parameterValue);
    }

    public List<String> getListParameter(String parameterName) {
        return (List<String>)parameterMap.get(parameterName);
    }

}
