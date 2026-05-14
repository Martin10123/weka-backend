package com.weka.weka.contract.response;

public record DatasetPreviewRow(
		Long passengerId,
		Integer survived,
		Integer pclass,
		String name,
		String sex,
		Double age,
		Integer sibSp,
		Integer parch,
		String ticket,
		Double fare,
		String cabin,
		String embarked
) {
}