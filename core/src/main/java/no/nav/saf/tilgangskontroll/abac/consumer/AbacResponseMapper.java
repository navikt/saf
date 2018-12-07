package no.nav.saf.tilgangskontroll.abac.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.nav.saf.tilgangskontroll.abac.dto.response.Advice;
import no.nav.saf.tilgangskontroll.abac.dto.response.AttributeAssignment;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.Obligation;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.exception.JsonUnmarshallingException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
class AbacResponseMapper {
	private static final Factory<Obligation> OBLIGATION_FACTORY = (id, assignments) -> Obligation.builder()
			.id(id)
			.attributeAssignments(assignments)
			.build();
	private static final Factory<Advice> ADVICE_FACTORY = (id, assignments) -> Advice.builder()
			.id(id)
			.attributeAssignments(assignments)
			.build();

	private static final ObjectMapper objectMapper = new ObjectMapper();

	XacmlResponse map(String json) {
		try {
			JsonNode node = objectMapper.readTree(json);

			Decision decision = Decision.findByValue(getDecision(node));
			List<Obligation> obligations = getObligationOrAdvice(node, "Obligations", OBLIGATION_FACTORY);
			List<Advice> advices = getObligationOrAdvice(node, "AssociatedAdvice", ADVICE_FACTORY);

			return new XacmlResponse(decision, decision, obligations, advices);
		} catch (IOException e) {
			throw new JsonUnmarshallingException("Failed to unmarshall json: \n" + json, e);
		}
	}

	private String getDecision(JsonNode node) {
		return getFieldValue(node.get("Response"), "Decision");
	}

	private <T> List<T> getObligationOrAdvice(JsonNode rootNode, String path, Factory<T> factory) {
		JsonNode node = rootNode.get("Response").get(path);
		List<T> items = new ArrayList<>();
		if (node instanceof ArrayNode) {
			for (JsonNode child : node) {
				String id = getFieldValue(child, "Id");
				List<AttributeAssignment> assignments = getAttributeAssignments(child.get("AttributeAssignment"));
				items.add(factory.create(id, assignments));
			}
		} else if (node instanceof ObjectNode) {
			String id = getFieldValue(node, "Id");
			List<AttributeAssignment> assignments = getAttributeAssignments(node.get("AttributeAssignment"));
			items.add(factory.create(id, assignments));
		}
		return items;
	}

	private List<AttributeAssignment> getAttributeAssignments(JsonNode node) {
		List<AttributeAssignment> result = new ArrayList<>();
		if (node instanceof ArrayNode) {
			for (JsonNode child : node) {
				result.add(mapAttributeAssignment(child));
			}
		} else if (node instanceof ObjectNode) {
			result.add(mapAttributeAssignment(node));
		}
		return result;
	}

	private AttributeAssignment mapAttributeAssignment(JsonNode node) {
		return AttributeAssignment.builder()
				.attributeId(getFieldValue(node, "AttributeId"))
				.value(getFieldValue(node, "Value"))
				.category(getFieldValue(node, "Category"))
				.dataType(getFieldValue(node, "DataType"))
				.build();
	}

	private String getFieldValue(JsonNode node, String field) {
		JsonNode subNode = node.get(field);
		if (subNode != null) {
			return subNode.asText();
		}
		return null;
	}

	private interface Factory<T> {
		T create(String id, List<AttributeAssignment> assignments);
	}
}