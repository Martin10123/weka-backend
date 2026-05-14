package com.weka.weka.service;

import com.weka.weka.contract.request.PassengerProfileRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;

@Service
public class TitanicRuleExtractionService {
	private static final Pattern CONDITION_PATTERN = Pattern.compile("(.+?)\\s*(<=|>=|=|<|>)\\s*(.+)");

	public List<String> extractRules(Classifier classifier, PassengerProfileRequest request, boolean survived) {
		List<RuleCandidate> candidates = parseRuleCandidates(classifier == null ? "" : classifier.toString());
		String targetLabel = survived ? "survived" : "not_survived";

		List<String> matchingRules = candidates.stream()
				.filter(candidate -> targetLabel.equalsIgnoreCase(candidate.predictedClass()))
				.filter(candidate -> matchesRequest(candidate.conditions(), request))
				.map(RuleCandidate::toReadableRule)
				.toList();

		if (!matchingRules.isEmpty()) {
			return matchingRules;
		}

		return candidates.stream()
				.filter(candidate -> targetLabel.equalsIgnoreCase(candidate.predictedClass()))
				.map(RuleCandidate::toReadableRule)
				.limit(5)
				.toList();
	}

	private List<RuleCandidate> parseRuleCandidates(String treeText) {
		List<RuleCandidate> candidates = new ArrayList<>();
		List<String> stack = new ArrayList<>();

		for (String rawLine : treeText.split("\\R")) {
			String trimmedLine = rawLine.stripTrailing();
			if (trimmedLine.isBlank()) {
				continue;
			}
			String lowerCaseLine = trimmedLine.toLowerCase(Locale.ROOT);
			if (lowerCaseLine.startsWith("number of leaves") || lowerCaseLine.startsWith("size of the tree")) {
				break;
			}
			if (trimmedLine.startsWith("j48") || trimmedLine.startsWith("===") || trimmedLine.startsWith("reduced error") || trimmedLine.startsWith("pruned tree")) {
				continue;
			}

			int depth = 0;
			while (trimmedLine.startsWith("|   ")) {
				depth++;
				trimmedLine = trimmedLine.substring(4);
			}

			while (stack.size() > depth) {
				stack.remove(stack.size() - 1);
			}

			String conditionPart = trimmedLine;
			String predictedClass = null;
			if (trimmedLine.contains(":")) {
				String[] parts = trimmedLine.split(":", 2);
				conditionPart = parts[0].trim();
				String classAndCount = parts[1].trim();
				predictedClass = classAndCount.split("\\s+")[0].trim();
				candidates.add(new RuleCandidate(new ArrayList<>(buildPath(stack, conditionPart)), predictedClass));
			} else {
				stack.add(conditionPart);
			}
		}

		return candidates;
	}

	private List<String> buildPath(List<String> stack, String condition) {
		List<String> path = new ArrayList<>(stack);
		if (condition != null && !condition.isBlank()) {
			path.add(condition.trim());
		}
		return path;
	}

	private boolean matchesRequest(List<String> conditions, PassengerProfileRequest request) {
		for (String condition : conditions) {
			if (!matchesCondition(condition, request)) {
				return false;
			}
		}
		return true;
	}

	private boolean matchesCondition(String condition, PassengerProfileRequest request) {
		Matcher matcher = CONDITION_PATTERN.matcher(condition.trim());
		if (!matcher.matches()) {
			return true;
		}

		String attribute = matcher.group(1).trim().toLowerCase(Locale.ROOT);
		String operator = matcher.group(2).trim();
		String rawExpectedValue = stripQuotes(matcher.group(3).trim());

		switch (attribute) {
			case "age":
				return compareNumeric(request.age(), operator, rawExpectedValue);
			case "passenger_class":
				return compareText(request.passengerClass() == null ? null : request.passengerClass().name(), operator, rawExpectedValue);
			case "sex":
				return compareText(request.sex() == null ? null : request.sex().toCsvValue(), operator, rawExpectedValue);
			case "traveling_alone":
				return compareText(Boolean.toString(request.travelingAlone()), operator, rawExpectedValue);
			case "embarked":
				return compareText(request.embarked() == null ? null : request.embarked().getCsvValue(), operator, rawExpectedValue);
			default:
				return true;
		}
	}

	private boolean compareNumeric(Double actual, String operator, String expectedValue) {
		if (actual == null) {
			return false;
		}
		double expected = Double.parseDouble(expectedValue);
		return switch (operator) {
			case "<=" -> actual <= expected;
			case ">=" -> actual >= expected;
			case "<" -> actual < expected;
			case ">" -> actual > expected;
			case "=" -> actual.equals(expected);
			default -> false;
		};
	}

	private boolean compareText(String actual, String operator, String expectedValue) {
		if (actual == null) {
			return false;
		}
		String normalizedActual = actual.trim().toLowerCase(Locale.ROOT);
		String normalizedExpected = expectedValue.trim().toLowerCase(Locale.ROOT);
		return switch (operator) {
			case "=" -> normalizedActual.equals(normalizedExpected);
			case "!=" -> !normalizedActual.equals(normalizedExpected);
			default -> false;
		};
	}

	private String stripQuotes(String value) {
		String stripped = value;
		if (stripped.startsWith("\"") && stripped.endsWith("\"") && stripped.length() >= 2) {
			stripped = stripped.substring(1, stripped.length() - 1);
		}
		return stripped;
	}

	private record RuleCandidate(List<String> conditions, String predictedClass) {
		private String toReadableRule() {
			if (conditions.isEmpty()) {
				return "then " + predictedClass;
			}
			return "if " + String.join(" AND ", conditions) + " then " + predictedClass;
		}
	}
}