package com.weka.weka.domain.titanic;

public record NormalizedTitanicRow(
		Long passengerId,
		Integer survived,
		PassengerClass passengerClass,
		Sex sex,
		Double age,
		Boolean travelingAlone,
		EmbarkationPort embarked
) {
}