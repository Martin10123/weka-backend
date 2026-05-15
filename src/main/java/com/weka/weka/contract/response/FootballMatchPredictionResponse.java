package com.weka.weka.contract.response;

public record FootballMatchPredictionResponse(
		String predictedResult,
		double homeWinProbability,
		double drawProbability,
		double awayWinProbability,
		double confidence,
		String insight
) {
}