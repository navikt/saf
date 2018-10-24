package no.nav.saf.tilgangskontroll.abac.service;

import no.nav.saf.tilgangskontroll.abac.consumer.AbacConsumer;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Advice;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.Obligation;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.exception.IndeterminateDecisionException;
import no.nav.saf.tilgangskontroll.abac.exception.UnhandledObligationException;
import no.nav.saf.tilgangskontroll.abac.service.advice.AdviceStrategy;
import no.nav.saf.tilgangskontroll.abac.service.common.AttributeStrategy;
import no.nav.saf.tilgangskontroll.abac.service.obligation.ObligationStrategy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AbacServiceImpl implements AbacService {

	private final AbacConsumer abacConsumer;
	private final List<ObligationStrategy> obligationStrategies;
	private final List<AdviceStrategy> adviceStrategies;

	public AbacServiceImpl(AbacConsumer abacConsumer,
						   List<ObligationStrategy> obligationStrategies,
						   List<AdviceStrategy> adviceStrategies) {
		this.abacConsumer = abacConsumer;
		this.obligationStrategies = new ArrayList<>(obligationStrategies);
		this.adviceStrategies = new ArrayList<>(adviceStrategies);
	}

	@Override
	public XacmlResponse evaluate(XacmlRequest request) {
		XacmlResponse response = abacConsumer.evaluate(request);
		response = assignResultBasedOnBias(request, response);

		handleObligations(response);
		handleAdvice(response);

		return response;
	}

	private XacmlResponse assignResultBasedOnBias(XacmlRequest request, XacmlResponse response) {
		if (response.getOriginalDecision() == Decision.INDETERMINATE && request.isFailOnIndeterminate()) {
			throw new IndeterminateDecisionException();
		} else if (response.getOriginalDecision() != Decision.PERMIT && response.getOriginalDecision() != Decision.DENY) {
			return new XacmlResponse(request.getBias(), response.getOriginalDecision(), response.getObligations(), response.getAdvices());
		}
		return response;
	}

	private void handleObligations(XacmlResponse response) {
		for (Obligation obligation : response.getObligations()) {
			ObligationStrategy strategy = findSupportedStrategy(obligation.getId(), obligationStrategies);
			if (strategy == null) {
				throw new UnhandledObligationException(obligation.getId());
			}
			strategy.perform(obligation);
		}
	}

	private void handleAdvice(XacmlResponse response) {
		for (Advice advice : response.getAdvices()) {
			AdviceStrategy strategy = findSupportedStrategy(advice.getId(), adviceStrategies);
			if (strategy != null) {
				strategy.perform(advice);
			}
		}
	}

	private <T extends AttributeStrategy<?>> T findSupportedStrategy(String id, List<T> strategies) {
		for (T strategy : strategies) {
			if (strategy.isSupported(id)) {
				return strategy;
			}
		}
		return null;
	}

}