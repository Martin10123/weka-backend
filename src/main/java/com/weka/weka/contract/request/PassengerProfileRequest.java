package com.weka.weka.contract.request;

import com.weka.weka.domain.titanic.EmbarkationPort;
import com.weka.weka.domain.titanic.PassengerClass;
import com.weka.weka.domain.titanic.Sex;

public record PassengerProfileRequest(
		Sex sex,
		Double age,
		PassengerClass passengerClass,
		boolean travelingAlone,
		EmbarkationPort embarked
) {
}