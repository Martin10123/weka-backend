package com.weka.weka.domain.football;

public record FootballMatchRow(
		String division,
		Double homeElo,
		Double awayElo,
		Double form3Home,
		Double form3Away,
		Double form5Home,
		Double form5Away,
		Double homeOdds,
		Double drawOdds,
		Double awayOdds,
		String ftResult
) {
}