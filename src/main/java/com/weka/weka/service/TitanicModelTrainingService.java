package com.weka.weka.service;

import com.weka.weka.contract.response.ModelTrainingResponse;
import com.weka.weka.domain.titanic.NormalizedTitanicDataset;
import com.weka.weka.domain.titanic.TitanicDataset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SerializationHelper;

@Service
public class TitanicModelTrainingService {
	private final TitanicDatasetReader titanicDatasetReader;
	private final TitanicDatasetNormalizer titanicDatasetNormalizer;
	private final TitanicWekaDatasetFactory titanicWekaDatasetFactory;
	private final TitanicExplanationService titanicExplanationService;

	public TitanicModelTrainingService(
			TitanicDatasetReader titanicDatasetReader,
			TitanicDatasetNormalizer titanicDatasetNormalizer,
			TitanicWekaDatasetFactory titanicWekaDatasetFactory,
			TitanicExplanationService titanicExplanationService
	) {
		this.titanicDatasetReader = titanicDatasetReader;
		this.titanicDatasetNormalizer = titanicDatasetNormalizer;
		this.titanicWekaDatasetFactory = titanicWekaDatasetFactory;
		this.titanicExplanationService = titanicExplanationService;
	}

	public ModelTrainingResponse train(MultipartFile file) {
		TitanicDataset dataset = titanicDatasetReader.read(file);
		NormalizedTitanicDataset normalizedDataset = titanicDatasetNormalizer.normalize(dataset);
		Instances trainingInstances = titanicWekaDatasetFactory.createTrainingInstances(normalizedDataset);
		if (trainingInstances.isEmpty()) {
			throw new IllegalStateException("No valid rows available for training");
		}

		try {
			J48 classifier = new J48();
			classifier.setConfidenceFactor(0.25f);
			classifier.setMinNumObj(2);
			classifier.buildClassifier(trainingInstances);

			Evaluation evaluation = new Evaluation(trainingInstances);
			evaluation.crossValidateModel(classifier, trainingInstances, 10, new Random(1));

			Object[] artifact = new Object[] { classifier, new Instances(trainingInstances, 0) };
			Path modelPath = getModelPath(dataset.sourceFileName());
			Files.createDirectories(modelPath.getParent());
			SerializationHelper.write(modelPath.toString(), artifact);
			String summary = evaluation.toSummaryString("J48 cross-validation summary", false);
			long usedRows = normalizedDataset.rows().size();
			int discardedRows = normalizedDataset.discardedRows();
			double accuracy = evaluation.pctCorrect();
			String fallbackInsight = buildTrainingInsightFallback(usedRows, discardedRows, accuracy);
			String insight = titanicExplanationService.explainTrainingResult(
					dataset.sourceFileName(),
					dataset.rows().size(),
					usedRows,
					discardedRows,
					accuracy,
					summary,
					fallbackInsight
			);

			return new ModelTrainingResponse(
					dataset.sourceFileName(),
					modelPath.toString(),
					dataset.rows().size(),
					usedRows,
					discardedRows,
					accuracy,
					insight,
					summary
			);
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to train Titanic model", exception);
		}
	}

	private String buildTrainingInsightFallback(long usedRows, int discardedRows, double accuracy) {
		String quality;
		if (accuracy >= 85.0) {
			quality = "alto";
		} else if (accuracy >= 75.0) {
			quality = "aceptable";
		} else {
			quality = "mejorable";
		}

		if (discardedRows == 0) {
			return "Entrenamiento completado con " + usedRows + " filas validas y sin descartes. "
					+ "La exactitud de validacion cruzada fue " + String.format("%.2f", accuracy)
					+ "%, lo que indica un desempeno " + quality + " para un primer modelo J48.";
		}

		return "Entrenamiento completado con " + usedRows + " filas validas y " + discardedRows + " filas descartadas en la normalizacion. "
				+ "La exactitud de validacion cruzada fue " + String.format("%.2f", accuracy)
				+ "%, lo que indica un desempeno " + quality + " para un primer modelo J48.";
	}

	private Path getModelPath(String sourceFileName) {
		String modelName = sourceFileName == null || sourceFileName.isBlank()
				? "titanic-j48.model"
				: sourceFileName.replaceAll("\\.[^.]+$", "") + "-j48.model";
			return Path.of("data", "models", modelName);
	}
}