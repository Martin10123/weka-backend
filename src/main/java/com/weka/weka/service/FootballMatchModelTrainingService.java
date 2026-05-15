package com.weka.weka.service;

import com.weka.weka.contract.response.ModelTrainingResponse;
import com.weka.weka.domain.football.FootballMatchDataset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;

@Service
public class FootballMatchModelTrainingService {
	private final FootballMatchDatasetReader footballMatchDatasetReader;
	private final FootballMatchWekaDatasetFactory footballMatchWekaDatasetFactory;

	public FootballMatchModelTrainingService(
			FootballMatchDatasetReader footballMatchDatasetReader,
			FootballMatchWekaDatasetFactory footballMatchWekaDatasetFactory
	) {
		this.footballMatchDatasetReader = footballMatchDatasetReader;
		this.footballMatchWekaDatasetFactory = footballMatchWekaDatasetFactory;
	}

	public ModelTrainingResponse train(MultipartFile file) {
		FootballMatchDataset dataset = footballMatchDatasetReader.read(file);
		Instances trainingInstances = footballMatchWekaDatasetFactory.createTrainingInstances(dataset);
		if (trainingInstances.isEmpty()) {
			throw new IllegalStateException("No valid football rows available for training");
		}

		try {
			RandomForest classifier = new RandomForest();
			// Use default RandomForest settings to avoid triggering Weka class discovery
			// (which can fail when scanning the classpath in some environments).
			classifier.buildClassifier(trainingInstances);

			double accuracy;
			String summary;
			if (trainingInstances.numInstances() >= 2) {
				Evaluation evaluation = new Evaluation(trainingInstances);
				int folds = Math.min(10, trainingInstances.numInstances());
				if (folds < 2) {
					folds = 2;
				}
				evaluation.crossValidateModel(classifier, trainingInstances, folds, new Random(1));
				accuracy = evaluation.pctCorrect();
				summary = evaluation.toSummaryString("RandomForest cross-validation summary", false);
			} else {
				accuracy = Double.NaN;
				summary = "Cross-validation skipped because the dataset has fewer than 2 valid rows.";
			}

			Object[] artifact = new Object[] { classifier, new Instances(trainingInstances, 0) };
			Path modelPath = getModelPath(dataset.sourceFileName());
			Files.createDirectories(modelPath.getParent());
			SerializationHelper.write(modelPath.toString(), artifact);

			long usedRows = trainingInstances.numInstances();
			int discardedRows = dataset.rows().size() - trainingInstances.numInstances();
			String fallbackInsight = buildTrainingInsightFallback(usedRows, discardedRows, accuracy);

			return new ModelTrainingResponse(
					dataset.sourceFileName(),
					modelPath.toString(),
					dataset.rows().size(),
					usedRows,
					discardedRows,
					accuracy,
					fallbackInsight,
					summary
			);
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to train football model", exception);
		}
	}

	private String buildTrainingInsightFallback(long usedRows, int discardedRows, double accuracy) {
		String quality;
		if (!Double.isNaN(accuracy) && accuracy >= 85.0) {
			quality = "alto";
		} else if (!Double.isNaN(accuracy) && accuracy >= 75.0) {
			quality = "aceptable";
		} else {
			quality = "mejorable";
		}

		String accuracyText = Double.isNaN(accuracy) ? "sin validacion cruzada" : String.format(Locale.ROOT, "%.2f%%", accuracy);
		if (discardedRows == 0) {
			return "Entrenamiento completado con " + usedRows + " filas validas y sin descartes. "
					+ "La exactitud de validacion cruzada fue " + accuracyText
					+ ", lo que indica un desempeno " + quality + " para el primer modelo de resultado.";
		}

		return "Entrenamiento completado con " + usedRows + " filas validas y " + discardedRows + " filas descartadas. "
				+ "La exactitud de validacion cruzada fue " + accuracyText
				+ ", lo que indica un desempeno " + quality + " para el primer modelo de resultado.";
	}

	private Path getModelPath(String sourceFileName) {
		String baseName = sourceFileName == null || sourceFileName.isBlank()
				? "football"
				: sourceFileName.replaceAll("\\.[^.]+$", "").replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase(Locale.ROOT);
		return Path.of("data", "models", "football-" + baseName + "-randomforest.model");
	}
}