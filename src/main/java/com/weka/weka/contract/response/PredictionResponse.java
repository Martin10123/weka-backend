package com.weka.weka.contract.response;

import java.util.List;

public record PredictionResponse(
		boolean survived,
		Double probability,
		List<String> rules,
		String insight,
		String narrative
) {
}