package com.weka.weka.contract.response;

public record DatasetNormalizationRow(
		Long passengerId,
		Integer survived,
		String passengerClass,
		String sex,
		Double age,
		Boolean travelingAlone,
		String embarked
) {
}