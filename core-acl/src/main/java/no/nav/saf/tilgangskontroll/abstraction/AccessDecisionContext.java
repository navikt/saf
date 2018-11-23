package no.nav.saf.tilgangskontroll.abstraction;

import java.util.concurrent.ConcurrentHashMap;

public class AccessDecisionContext {
    public enum AccessDecision {ALLOW, DENY};

    private ConcurrentHashMap<String, AccessDecision> accessDecisions;

    public boolean hasAccessDecisionFor(String xacmlRequest) {
        return accessDecisions.containsKey(xacmlRequest);
    }

    public AccessDecision getAccessDecisionFor(String xacmlRequest) {
        return accessDecisions.getOrDefault(xacmlRequest, AccessDecision.DENY);
    }

    public void storeAccessDecision(String xacmlRequest, AccessDecision accessDecision) {
        accessDecisions.put(xacmlRequest, accessDecision);
    }
}
