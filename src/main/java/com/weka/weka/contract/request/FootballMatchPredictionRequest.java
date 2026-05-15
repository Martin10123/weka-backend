package com.weka.weka.contract.request;

public record FootballMatchPredictionRequest(
		String division,
		double homeElo,
		double awayElo,
		double form3Home,
		double form3Away,
		double form5Home,
		double form5Away,
		double homeOdds,
		double drawOdds,
		double awayOdds
) {
}