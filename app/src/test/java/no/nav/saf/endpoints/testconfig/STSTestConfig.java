package no.nav.saf.endpoints.testconfig;

import no.nav.saf.config.ServiceuserAlias;
import no.nav.saf.integration.sts.STSConfig;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Profile("itest")
public class STSTestConfig extends STSConfig {

	public STSTestConfig(ServiceuserAlias serviceuserAlias) {
		super(serviceuserAlias);
	}

	@Override
	public void configureSTS(Object port) {
	}
}
