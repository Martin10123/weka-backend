package com.weka.weka.domain.football;

import java.util.List;

public record FootballMatchDataset(
		String sourceFileName,
		List<FootballMatchRow> rows
) {
}