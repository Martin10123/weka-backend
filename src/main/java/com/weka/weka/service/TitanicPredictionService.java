package com.weka.weka.service;

import com.weka.weka.contract.request.PassengerProfileRequest;
import com.weka.weka.contract.response.PredictionResponse;
import com.weka.weka.domain.model.TitanicModelArtifact;
import java.util.List;
import org.springframework.stereotype.Service;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

@Service
public class TitanicPredictionService {
	private final TitanicModelLoaderService titanicModelLoaderService;
	private final TitanicRuleExtractionService titanicRuleExtractionService;

	public TitanicPredictionService(TitanicModelLoaderService titanicModelLoaderService, TitanicRuleExtractionService titanicRuleExtractionService) {
		this.titanicModelLoaderService = titanicModelLoaderService;
		this.titanicRuleExtractionService = titanicRuleExtractionService;
	}

	public PredictionResponse predict(PassengerProfileRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Passenger profile is required");
		}

		TitanicModelArtifact modelArtifact = titanicModelLoaderService.loadLatestModel();
		Instances structure = new Instances(modelArtifact.structure(), 0);
		Instance instance = buildInstance(structure, request);

		try {
			double[] distribution = modelArtifact.classifier().distributionForInstance(instance);
			int predictedIndex = distribution[1] >= distribution[0] ? 1 : 0;
			boolean survived = predictedIndex == 1;
			double probability = distribution[predictedIndex];
			List<String> rules = titanicRuleExtractionService.extractRules(modelArtifact.classifier(), request, survived);

			return new PredictionResponse(
					survived,
					probability,
					rules,
					selectedInsight(survived, probability, rules),
					"Narrative will be added in the explanation phase."
			);
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to predict Titanic survival", exception);
		}
	}

	private String selectedInsight(boolean survived, double probability, List<String> rules) {
		String outcome = survived ? "survived" : "did not survive";
		String ruleSummary = rules.isEmpty() ? "No matching rule could be extracted." : "Selected rule: " + rules.get(0);
		return "Prediction result: passenger " + outcome + " with probability " + Math.round(probability * 100.0) + "% . " + ruleSummary;
	}

	private Instance buildInstance(Instances structure, PassengerProfileRequest request) {
		DenseInstance instance = new DenseInstance(structure.numAttributes());
		instance.setDataset(structure);

		if (request.age() != null) {
			instance.setValue(structure.attribute("age"), request.age());
		}
		if (request.passengerClass() != null) {
			instance.setValue(structure.attribute("passenger_class"), request.passengerClass().name());
		}
		if (request.sex() != null) {
			instance.setValue(structure.attribute("sex"), request.sex().toCsvValue());
		}
		instance.setValue(structure.attribute("traveling_alone"), request.travelingAlone() ? "true" : "false");
		if (request.embarked() != null) {
			instance.setValue(structure.attribute("embarked"), request.embarked().getCsvValue());
		}

		instance.setMissing(structure.classIndex());
		return instance;
	}
}