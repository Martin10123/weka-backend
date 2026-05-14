package com.weka.weka.domain.titanic;

import java.util.List;

public record NormalizedTitanicDataset(
		String sourceFileName,
		List<NormalizedTitanicRow> rows,
		int discardedRows
) {
}