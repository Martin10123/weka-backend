package com.weka.weka.domain.titanic;

import java.util.List;

public record TitanicDataset(
		String sourceFileName,
		List<TitanicCsvRow> rows
) {
}