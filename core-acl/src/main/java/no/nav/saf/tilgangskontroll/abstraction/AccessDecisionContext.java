package no.nav.saf.tilgangskontroll.abstraction;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread safe store of earlier access decision in current request
 */
public class AccessDecisionContext {
    public enum AccessDecision {ALLOW, DENY};

    private ConcurrentHashMap<String, AccessDecision> accessDecisions;

    public boolean hasAccessDecisionFor(String accessRequest) {
        return accessDecisions.containsKey(accessRequest);
    }

    public AccessDecision getAccessDecisionFor(String accesslRequest) {
        return accessDecisions.getOrDefault(accesslRequest, AccessDecision.DENY);
    }

    public void storeAccessDecision(String accesslRequest, AccessDecision accessDecision) {
        accessDecisions.put(accesslRequest, accessDecision);
    }
}
