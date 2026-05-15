package com.weka.weka.service;

import com.weka.weka.domain.football.FootballMatchDataset;
import com.weka.weka.domain.football.FootballMatchRow;
import com.weka.weka.domain.model.FootballMatchModelArtifact;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

@Service
public class FootballMatchWekaDatasetFactory {
	private static final String RELATION_NAME = "football_match_result";
	private static final String UNKNOWN_DIVISION = "UNKNOWN";
	private static final List<String> RESULT_VALUES = List.of("Home Win", "Draw", "Away Win");

	public Instances createTrainingInstances(FootballMatchDataset dataset) {
		Instances instances = createEmptyStructure(extractDivisions(dataset));
		for (FootballMatchRow row : dataset.rows()) {
			Instance instance = toInstance(instances, row);
			if (instance != null) {
				instances.add(instance);
			}
		}
		return instances;
	}

	public FootballMatchModelArtifact createModelArtifact(Classifier classifier, Instances trainingInstances) {
		return new FootballMatchModelArtifact(classifier, new Instances(trainingInstances, 0));
	}

	public Instances createEmptyStructure(List<String> divisions) {
		ArrayList<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute("Division", buildDivisionValues(divisions)));
		attributes.add(new Attribute("HomeElo"));
		attributes.add(new Attribute("AwayElo"));
		attributes.add(new Attribute("Form3Home"));
		attributes.add(new Attribute("Form3Away"));
		attributes.add(new Attribute("Form5Home"));
		attributes.add(new Attribute("Form5Away"));
		attributes.add(new Attribute("OddHome"));
		attributes.add(new Attribute("OddDraw"));
		attributes.add(new Attribute("OddAway"));
		attributes.add(new Attribute("FTResult", RESULT_VALUES));

		Instances instances = new Instances(RELATION_NAME, attributes, 0);
		instances.setClassIndex(instances.numAttributes() - 1);
		return instances;
	}

	private Instance toInstance(Instances structure, FootballMatchRow row) {
		if (row == null
				|| row.division() == null
				|| row.homeElo() == null
				|| row.awayElo() == null
				|| row.form3Home() == null
				|| row.form3Away() == null
				|| row.form5Home() == null
				|| row.form5Away() == null
				|| row.homeOdds() == null
				|| row.drawOdds() == null
				|| row.awayOdds() == null
				|| row.ftResult() == null) {
			return null;
		}

		double[] values = new double[structure.numAttributes()];
		for (int index = 0; index < values.length; index++) {
			values[index] = Utils.missingValue();
		}

		DenseInstance instance = new DenseInstance(1.0, values);
		instance.setDataset(structure);
		instance.setValue(structure.attribute("Division"), normalizeDivision(structure, row.division()));
		instance.setValue(structure.attribute("HomeElo"), row.homeElo());
		instance.setValue(structure.attribute("AwayElo"), row.awayElo());
		instance.setValue(structure.attribute("Form3Home"), row.form3Home());
		instance.setValue(structure.attribute("Form3Away"), row.form3Away());
		instance.setValue(structure.attribute("Form5Home"), row.form5Home());
		instance.setValue(structure.attribute("Form5Away"), row.form5Away());
		instance.setValue(structure.attribute("OddHome"), row.homeOdds());
		instance.setValue(structure.attribute("OddDraw"), row.drawOdds());
		instance.setValue(structure.attribute("OddAway"), row.awayOdds());
		String classLabel = toClassLabel(row.ftResult());
		if (classLabel == null || structure.classAttribute().indexOfValue(classLabel) < 0) {
			return null;
		}
		instance.setValue(structure.classAttribute(), classLabel);
		return instance;
	}

	private List<String> extractDivisions(FootballMatchDataset dataset) {
		Set<String> divisions = new LinkedHashSet<>();
		divisions.add(UNKNOWN_DIVISION);
		for (FootballMatchRow row : dataset.rows()) {
			if (row != null && row.division() != null && !row.division().isBlank()) {
				divisions.add(row.division().trim());
			}
		}
		return new ArrayList<>(divisions);
	}

	private List<String> buildDivisionValues(List<String> divisions) {
		Set<String> values = new LinkedHashSet<>();
		values.add(UNKNOWN_DIVISION);
		if (divisions != null) {
			for (String division : divisions) {
				if (division != null && !division.isBlank()) {
					values.add(division.trim());
				}
			}
		}
		return new ArrayList<>(values);
	}

	private String normalizeDivision(Instances structure, String division) {
		String normalized = division == null || division.isBlank() ? UNKNOWN_DIVISION : division.trim();
		if (structure.attribute("Division").indexOfValue(normalized) >= 0) {
			return normalized;
		}
		return UNKNOWN_DIVISION;
	}

	private String toClassLabel(String rawResult) {
		if (rawResult == null) {
			return null;
		}
		String normalized = rawResult.trim();
		return switch (normalized.toUpperCase()) {
			case "H" -> "Home Win";
			case "D" -> "Draw";
			case "A" -> "Away Win";
			default -> switch (normalized.toLowerCase()) {
				case "home win" -> "Home Win";
				case "draw" -> "Draw";
				case "away win" -> "Away Win";
				default -> null;
			};
		};
	}
}