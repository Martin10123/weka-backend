package com.weka.weka.domain.titanic;

public record TitanicCsvRow(
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