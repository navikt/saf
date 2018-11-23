package no.nav.saf.tilgangskontroll.abstraction;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParameterContext {
    private final ConcurrentHashMap<String, Object> parameterMap;

    public ParameterContext() {
        parameterMap = new ConcurrentHashMap<>();
    }

    public ParameterContext(Map<String, ? extends Object> parameterMap) {
        this.parameterMap = new ConcurrentHashMap<>(parameterMap);
    }

    public void addSearchParameters(Map<String, ? extends Object> parentSearchParameters) {
        parameterMap.putAll(parentSearchParameters);
    }

    public void addStringSearchParameter(String parameterName, String parameterValue) {
        parameterMap.put(parameterName, parameterValue);
    }

    public void addListSearchParameter(String parameterName, List<String> valueList) {
        parameterMap.put(parameterName, valueList);
    }

    public String getStringParameter(String parameterName) {
        return parameterMap.containsKey(parameterName) ? parameterMap.get(parameterName).toString() : null;
    }

    public List<String> getListParameter(String parameterName) {
        return (List<String>)parameterMap.get(parameterName);
    }

}
