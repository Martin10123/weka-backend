package com.weka.weka.service;

import com.weka.weka.domain.model.TitanicModelArtifact;
import com.weka.weka.domain.titanic.NormalizedTitanicDataset;
import com.weka.weka.domain.titanic.NormalizedTitanicRow;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

@Service
public class TitanicWekaDatasetFactory {
	private static final String RELATION_NAME = "titanic_survival";

	public Instances createTrainingInstances(NormalizedTitanicDataset dataset) {
		Instances instances = createEmptyStructure();
		for (NormalizedTitanicRow row : dataset.rows()) {
			instances.add(toInstance(instances, row));
		}
		return instances;
	}

	public TitanicModelArtifact createModelArtifact(weka.classifiers.Classifier classifier, Instances trainingInstances) {
		return new TitanicModelArtifact(classifier, new Instances(trainingInstances, 0));
	}

	public Instances createEmptyStructure() {
		ArrayList<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute("age"));
		attributes.add(new Attribute("passenger_class", List.of("FIRST", "SECOND", "THIRD")));
		attributes.add(new Attribute("sex", List.of("male", "female")));
		attributes.add(new Attribute("traveling_alone", List.of("false", "true")));
		attributes.add(new Attribute("embarked", List.of("S", "C", "Q")));
		attributes.add(new Attribute("survived", List.of("not_survived", "survived")));

		Instances instances = new Instances(RELATION_NAME, attributes, 0);
		instances.setClassIndex(instances.numAttributes() - 1);
		return instances;
	}

	private Instance toInstance(Instances structure, NormalizedTitanicRow row) {
		double[] values = new double[structure.numAttributes()];
		for (int index = 0; index < values.length; index++) {
			values[index] = Utils.missingValue();
		}

		DenseInstance instance = new DenseInstance(1.0, values);
		instance.setDataset(structure);

		if (row.age() != null) {
			instance.setValue(structure.attribute("age"), row.age());
		}
		instance.setValue(structure.attribute("passenger_class"), row.passengerClass().name());
		instance.setValue(structure.attribute("sex"), row.sex().toCsvValue());
		if (row.travelingAlone() != null) {
			instance.setValue(structure.attribute("traveling_alone"), row.travelingAlone() ? "true" : "false");
		}
		if (row.embarked() != null) {
			instance.setValue(structure.attribute("embarked"), row.embarked().getCsvValue());
		}
		instance.setValue(structure.classAttribute(), row.survived() == 1 ? "survived" : "not_survived");
		return instance;
	}
}