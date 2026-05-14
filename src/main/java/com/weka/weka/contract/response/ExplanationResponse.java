package com.weka.weka.contract.response;

import java.util.List;

public record ExplanationResponse(
		boolean survived,
		Double probability,
		List<String> rules,
		String narrative,
		String provider
) {
}