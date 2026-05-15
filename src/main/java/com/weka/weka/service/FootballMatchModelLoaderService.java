package com.weka.weka.service;

import com.weka.weka.domain.model.FootballMatchModelArtifact;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;

@Service
public class FootballMatchModelLoaderService {
	private static final Path MODELS_DIRECTORY = Path.of("data", "models");

	public FootballMatchModelArtifact loadLatestModel() {
		Path modelPath = findLatestModelPath()
				.orElseThrow(() -> new IllegalStateException("No trained football model found in data/models"));

		try {
			Object rawModel = SerializationHelper.read(modelPath.toString());
			return toModelArtifact(rawModel, modelPath);
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to load football model from " + modelPath, exception);
		}
	}

	private FootballMatchModelArtifact toModelArtifact(Object rawModel, Path modelPath) {
		if (rawModel instanceof FootballMatchModelArtifact artifact) {
			return artifact;
		}

		if (rawModel instanceof Object[] values
				&& values.length == 2
				&& values[0] instanceof Classifier classifier
				&& values[1] instanceof Instances structure) {
			return new FootballMatchModelArtifact(classifier, structure);
		}

		try {
			Method classifierMethod = rawModel.getClass().getMethod("classifier");
			Method structureMethod = rawModel.getClass().getMethod("structure");
			Object classifierValue = classifierMethod.invoke(rawModel);
			Object structureValue = structureMethod.invoke(rawModel);

			if (classifierValue instanceof Classifier classifier && structureValue instanceof Instances structure) {
				return new FootballMatchModelArtifact(classifier, structure);
			}
		} catch (Exception ignored) {
			// Fall through to the explicit error below.
		}

		throw new IllegalStateException("Unsupported football model artifact format in " + modelPath + ". Retrain the model using POST /football/models/train");
	}

	private Optional<Path> findLatestModelPath() {
		if (!Files.exists(MODELS_DIRECTORY)) {
			return Optional.empty();
		}

		try (var paths = Files.list(MODELS_DIRECTORY)) {
			return paths
					.filter(path -> {
						String fileName = path.getFileName().toString().toLowerCase();
						return fileName.endsWith(".model") && fileName.startsWith("football-");
					})
					.max(Comparator.comparingLong(path -> path.toFile().lastModified()));
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to inspect football models directory", exception);
		}
	}
}