package com.weka.weka.contract.response;

import java.util.List;

public record DatasetPreviewResponse(
		String sourceFileName,
		String detectedFormat,
		long totalRows,
		int limit,
		List<DatasetPreviewRow> rows
) {
}