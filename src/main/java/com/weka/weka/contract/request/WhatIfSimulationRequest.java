package com.weka.weka.contract.request;

import com.weka.weka.domain.titanic.EmbarkationPort;
import com.weka.weka.domain.titanic.PassengerClass;
import com.weka.weka.domain.titanic.Sex;

public record WhatIfSimulationRequest(
		PassengerProfileRequest baseProfile,
		Sex sex,
		Double age,
		PassengerClass passengerClass,
		Boolean travelingAlone,
		EmbarkationPort embarked
) {
}