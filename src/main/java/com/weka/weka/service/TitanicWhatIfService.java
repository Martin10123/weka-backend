package com.weka.weka.service;

import com.weka.weka.contract.request.PassengerProfileRequest;
import com.weka.weka.contract.request.WhatIfSimulationRequest;
import com.weka.weka.contract.response.PredictionResponse;
import com.weka.weka.contract.response.WhatIfSimulationResponse;
import com.weka.weka.domain.titanic.EmbarkationPort;
import com.weka.weka.domain.titanic.PassengerClass;
import com.weka.weka.domain.titanic.Sex;
import org.springframework.stereotype.Service;

@Service
public class TitanicWhatIfService {
	private final TitanicPredictionService titanicPredictionService;

	public TitanicWhatIfService(TitanicPredictionService titanicPredictionService) {
		this.titanicPredictionService = titanicPredictionService;
	}

	public WhatIfSimulationResponse simulate(WhatIfSimulationRequest request) {
		if (request == null || request.baseProfile() == null) {
			throw new IllegalArgumentException("Base passenger profile is required");
		}

		PassengerProfileRequest originalProfile = request.baseProfile();
		PassengerProfileRequest modifiedProfile = new PassengerProfileRequest(
				pickSex(request.sex(), originalProfile.sex()),
				request.age() != null ? request.age() : originalProfile.age(),
				pickPassengerClass(request.passengerClass(), originalProfile.passengerClass()),
				request.travelingAlone() != null ? request.travelingAlone() : originalProfile.travelingAlone(),
				pickEmbarked(request.embarked(), originalProfile.embarked())
		);

		PredictionResponse original = titanicPredictionService.predict(originalProfile);
		PredictionResponse modified = titanicPredictionService.predict(modifiedProfile);
		double delta = modified.probability() - original.probability();

		return new WhatIfSimulationResponse(
				original,
				modified,
				delta,
				buildSummary(original, modified, delta)
		);
	}

	private Sex pickSex(Sex overrideValue, Sex baseValue) {
		return overrideValue != null ? overrideValue : baseValue;
	}

	private PassengerClass pickPassengerClass(PassengerClass overrideValue, PassengerClass baseValue) {
		return overrideValue != null ? overrideValue : baseValue;
	}

	private EmbarkationPort pickEmbarked(EmbarkationPort overrideValue, EmbarkationPort baseValue) {
		return overrideValue != null ? overrideValue : baseValue;
	}

	private String buildSummary(PredictionResponse original, PredictionResponse modified, double delta) {
		String direction = delta >= 0 ? "increase" : "decrease";
		String originalOutcome = original.survived() ? "survive" : "not survive";
		String modifiedOutcome = modified.survived() ? "survive" : "not survive";
		return "Original profile would likely " + originalOutcome + ". Modified profile would likely " + modifiedOutcome + ". Probability " + direction + " of " + Math.round(Math.abs(delta) * 100.0) + "% points.";
	}
}