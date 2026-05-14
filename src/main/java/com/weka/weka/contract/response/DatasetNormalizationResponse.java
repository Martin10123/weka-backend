package com.weka.weka.contract.response;

import java.util.List;

public record DatasetNormalizationResponse(
		String sourceFileName,
		String detectedFormat,
		long totalRows,
		long normalizedRows,
		int discardedRows,
		List<DatasetNormalizationRow> rows
) {
}