package no.nav.saf.tilgangskontroll.abac.service;

import no.nav.saf.tilgangskontroll.abac.consumer.AbacConsumer;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Advice;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.exception.IndeterminateDecisionException;
import no.nav.saf.tilgangskontroll.abac.service.advice.AdviceStrategy;
import no.nav.saf.tilgangskontroll.abac.service.common.AttributeStrategy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AbacServiceImpl implements AbacService {

	private final AbacConsumer abacConsumer;
	private final List<AdviceStrategy> adviceStrategies;

	public AbacServiceImpl(AbacConsumer abacConsumer,
						   List<AdviceStrategy> adviceStrategies) {
		this.abacConsumer = abacConsumer;
		this.adviceStrategies = new ArrayList<>(adviceStrategies);
	}

	@Override
	public XacmlResponse evaluate(XacmlRequest request) {
		XacmlResponse response = abacConsumer.evaluate(request);
		response = assignResultBasedOnBias(request, response);

		handleAdvice(request, response);

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

	private void handleAdvice(XacmlRequest request, XacmlResponse response) {
		for (Advice advice : response.getAdvices()) {
			AdviceStrategy strategy = findSupportedStrategy(advice.getId(), adviceStrategies);
			if (strategy != null) {
				strategy.perform(advice, request, response);
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