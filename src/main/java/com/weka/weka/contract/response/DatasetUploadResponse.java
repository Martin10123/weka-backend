package com.weka.weka.contract.response;

public record DatasetUploadResponse(
		String sourceFileName,
		String detectedFormat,
		long totalRows
) {
}