package com.weka.weka.service;

import com.weka.weka.contract.request.FootballMatchPredictionRequest;
import com.weka.weka.contract.response.FootballMatchPredictionResponse;
import com.weka.weka.domain.model.FootballMatchModelArtifact;
import java.util.Locale;
import org.springframework.stereotype.Service;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

@Service
public class FootballMatchPredictionService {
	private final FootballMatchModelLoaderService footballMatchModelLoaderService;

	public FootballMatchPredictionService(FootballMatchModelLoaderService footballMatchModelLoaderService) {
		this.footballMatchModelLoaderService = footballMatchModelLoaderService;
	}

	public FootballMatchPredictionResponse predict(FootballMatchPredictionRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Football match request is required");
		}
		if (request.division() == null || request.division().isBlank()) {
			throw new IllegalArgumentException("Division is required");
		}

		FootballMatchModelArtifact modelArtifact = footballMatchModelLoaderService.loadLatestModel();
		Instances structure = new Instances(modelArtifact.structure(), 0);
		Instance instance = buildInstance(structure, request);

		try {
			double[] distribution = modelArtifact.classifier().distributionForInstance(instance);
			int predictedIndex = indexOfMax(distribution);
			String predictedResult = structure.classAttribute().value(predictedIndex);

			double homeWinProbability = probabilityForLabel(structure, distribution, "Home Win");
			double drawProbability = probabilityForLabel(structure, distribution, "Draw");
			double awayWinProbability = probabilityForLabel(structure, distribution, "Away Win");

			return new FootballMatchPredictionResponse(
					predictedResult,
					homeWinProbability,
					drawProbability,
					awayWinProbability,
					distribution[predictedIndex],
					buildInsight(request, predictedResult, distribution[predictedIndex], homeWinProbability, drawProbability, awayWinProbability)
			);
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to predict football match result", exception);
		}
	}

	private Instance buildInstance(Instances structure, FootballMatchPredictionRequest request) {
		DenseInstance instance = new DenseInstance(structure.numAttributes());
		instance.setDataset(structure);
		instance.setValue(structure.attribute("Division"), normalizeDivision(structure, request.division()));
		instance.setValue(structure.attribute("HomeElo"), request.homeElo());
		instance.setValue(structure.attribute("AwayElo"), request.awayElo());
		instance.setValue(structure.attribute("Form3Home"), request.form3Home());
		instance.setValue(structure.attribute("Form3Away"), request.form3Away());
		instance.setValue(structure.attribute("Form5Home"), request.form5Home());
		instance.setValue(structure.attribute("Form5Away"), request.form5Away());
		instance.setValue(structure.attribute("OddHome"), request.homeOdds());
		instance.setValue(structure.attribute("OddDraw"), request.drawOdds());
		instance.setValue(structure.attribute("OddAway"), request.awayOdds());
		instance.setMissing(structure.classIndex());
		return instance;
	}

	private String normalizeDivision(Instances structure, String division) {
		String normalized = division == null || division.isBlank() ? "UNKNOWN" : division.trim();
		if (structure.attribute("Division").indexOfValue(normalized) >= 0) {
			return normalized;
		}
		return "UNKNOWN";
	}

	private double probabilityForLabel(Instances structure, double[] distribution, String label) {
		int index = structure.classAttribute().indexOfValue(label);
		return index < 0 || index >= distribution.length ? 0.0 : distribution[index];
	}

	private int indexOfMax(double[] distribution) {
		int bestIndex = 0;
		for (int index = 1; index < distribution.length; index++) {
			if (distribution[index] > distribution[bestIndex]) {
				bestIndex = index;
			}
		}
		return bestIndex;
	}

	private String buildInsight(
			FootballMatchPredictionRequest request,
			String predictedResult,
			double confidence,
			double homeWinProbability,
			double drawProbability,
			double awayWinProbability
	) {
		double eloDelta = request.homeElo() - request.awayElo();
		double formHome = (request.form3Home() + request.form5Home()) / 2.0;
		double formAway = (request.form3Away() + request.form5Away()) / 2.0;
		double formDelta = formHome - formAway;
		return String.format(
				Locale.ROOT,
				"Prediccion: %s con %.1f%% de confianza. Elo local-vs-visitante: %+,.1f. Diferencia de forma: %+,.2f. Probabilidades [H: %.1f%%, D: %.1f%%, A: %.1f%%].",
				predictedResult,
				confidence * 100.0,
				eloDelta,
				formDelta,
				homeWinProbability * 100.0,
				drawProbability * 100.0,
				awayWinProbability * 100.0
		);
	}
}