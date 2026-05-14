package com.weka.weka.contract.response;

public record ModelTrainingResponse(
		String sourceFileName,
		String modelFilePath,
		long totalRows,
		long usedRows,
		int discardedRows,
		double crossValidationAccuracy,
		String insight,
		String summary
) {
}