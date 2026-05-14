package com.weka.weka.contract.response;

import java.util.List;

public record DatasetValidationResponse(
		boolean valid,
		String detectedFormat,
		String message,
		List<String> expectedColumns,
		List<String> actualColumns
) {
}