package com.weka.weka.domain.model;

import java.io.Serializable;
import weka.classifiers.Classifier;
import weka.core.Instances;

public record TitanicModelArtifact(
		Classifier classifier,
		Instances structure
) implements Serializable {
}