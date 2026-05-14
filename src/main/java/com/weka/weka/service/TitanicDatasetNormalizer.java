package com.weka.weka.service;

import com.weka.weka.domain.titanic.EmbarkationPort;
import com.weka.weka.domain.titanic.NormalizedTitanicDataset;
import com.weka.weka.domain.titanic.NormalizedTitanicRow;
import com.weka.weka.domain.titanic.PassengerClass;
import com.weka.weka.domain.titanic.Sex;
import com.weka.weka.domain.titanic.TitanicCsvRow;
import com.weka.weka.domain.titanic.TitanicDataset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TitanicDatasetNormalizer {

	public NormalizedTitanicDataset normalize(TitanicDataset dataset) {
		if (dataset == null) {
			throw new IllegalArgumentException("Dataset is required");
		}

		List<NormalizedTitanicRow> normalizedRows = new ArrayList<>();
		int discardedRows = 0;

		for (TitanicCsvRow row : dataset.rows()) {
			NormalizedTitanicRow normalizedRow = normalizeRow(row);
			if (normalizedRow == null) {
				discardedRows++;
				continue;
			}
			normalizedRows.add(normalizedRow);
		}

		return new NormalizedTitanicDataset(dataset.sourceFileName(), normalizedRows, discardedRows);
	}

	private NormalizedTitanicRow normalizeRow(TitanicCsvRow row) {
		if (row == null || row.passengerId() == null || row.survived() == null || row.pclass() == null || row.sex() == null) {
			return null;
		}

		PassengerClass passengerClass;
		Sex sex;
		EmbarkationPort embarked;
		try {
			passengerClass = PassengerClass.fromCsvValue(row.pclass());
			sex = Sex.fromCsvValue(row.sex());
			embarked = row.embarked() == null ? null : EmbarkationPort.fromCsvValue(row.embarked());
		} catch (IllegalArgumentException exception) {
			return null;
		}

		Boolean travelingAlone = buildTravelingAlone(row.sibSp(), row.parch());

		return new NormalizedTitanicRow(
				row.passengerId(),
				row.survived(),
				passengerClass,
				sex,
				row.age(),
				travelingAlone,
				embarked
		);
	}

	private Boolean buildTravelingAlone(Integer sibSp, Integer parch) {
		if (sibSp == null && parch == null) {
			return null;
		}

		int siblingsSpouses = sibSp == null ? 0 : sibSp;
		int parentsChildren = parch == null ? 0 : parch;
		return siblingsSpouses == 0 && parentsChildren == 0;
	}
}