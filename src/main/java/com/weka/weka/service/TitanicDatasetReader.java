package com.weka.weka.service;

import com.weka.weka.domain.titanic.TitanicCsvRow;
import com.weka.weka.domain.titanic.TitanicDataset;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TitanicDatasetReader {

	public TitanicDataset read(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Dataset file is required");
		}

		try {
			String fileName = file.getOriginalFilename();
			String lowerCaseName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
			if (lowerCaseName.endsWith(".arff") || looksLikeArff(file)) {
				return readArff(file);
			}

			return readCsv(file);
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to read Titanic dataset", exception);
		}
	}

	private TitanicDataset readCsv(MultipartFile file) throws IOException {
		try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
				CSVParser parser = CSVFormat.DEFAULT.builder()
						.setHeader()
						.setSkipHeaderRecord(true)
						.setTrim(true)
						.build()
						.parse(reader)) {

			List<TitanicCsvRow> rows = new ArrayList<>();
			for (CSVRecord record : parser) {
				rows.add(toRow(record));
			}

			return new TitanicDataset(file.getOriginalFilename(), rows);
		}
	}

	private TitanicDataset readArff(MultipartFile file) throws IOException {
		Path tempFile = Files.createTempFile("titanic-upload-", ".arff");
		try {
			file.transferTo(tempFile);
			ArffLoader loader = new ArffLoader();
			loader.setFile(tempFile.toFile());
			Instances instances = loader.getDataSet();
			if (instances.classIndex() < 0 && instances.numAttributes() > 1) {
				instances.setClassIndex(1);
			}

			List<TitanicCsvRow> rows = new ArrayList<>();
			for (int index = 0; index < instances.numInstances(); index++) {
				rows.add(toRow(instances.instance(index)));
			}

			return new TitanicDataset(file.getOriginalFilename(), rows);
		} finally {
			Files.deleteIfExists(tempFile);
		}
	}

	private TitanicCsvRow toRow(CSVRecord record) {
		return new TitanicCsvRow(
				parseLong(record, "PassengerId"),
				parseInteger(record, "Survived"),
				parseInteger(record, "Pclass"),
				getString(record, "Name"),
				getString(record, "Sex"),
				parseDouble(record, "Age"),
				parseInteger(record, "SibSp"),
				parseInteger(record, "Parch"),
				getString(record, "Ticket"),
				parseDouble(record, "Fare"),
				getString(record, "Cabin"),
				getString(record, "Embarked")
		);
	}

	private TitanicCsvRow toRow(Instance instance) {
		return new TitanicCsvRow(
				readLong(instance, 0),
				readInteger(instance, 1),
				readInteger(instance, 2),
				readString(instance, 3),
				readString(instance, 4),
				readDouble(instance, 5),
				readInteger(instance, 6),
				readInteger(instance, 7),
				readString(instance, 8),
				readDouble(instance, 9),
				readString(instance, 10),
				readString(instance, 11)
		);
	}

	private boolean looksLikeArff(MultipartFile file) {
		try (InputStream inputStream = file.getInputStream()) {
			String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
			String lowerContent = content.toLowerCase(Locale.ROOT);
			return lowerContent.startsWith("@relation") || lowerContent.contains("\n@attribute ") || lowerContent.contains("\r\n@attribute ");
		} catch (IOException exception) {
			return false;
		}
	}

	private String getString(CSVRecord record, String column) {
		if (!record.isMapped(column)) {
			return null;
		}
		String value = record.get(column);
		if (value == null || value.isBlank()) {
			return null;
		}
		return value;
	}

	private Integer parseInteger(CSVRecord record, String column) {
		String value = getString(record, column);
		return value == null ? null : Integer.valueOf(value);
	}

	private Long parseLong(CSVRecord record, String column) {
		String value = getString(record, column);
		return value == null ? null : Long.valueOf(value);
	}

	private Double parseDouble(CSVRecord record, String column) {
		String value = getString(record, column);
		return value == null ? null : Double.valueOf(value);
	}

	private String readString(Instance instance, int attributeIndex) {
		if (instance.isMissing(attributeIndex)) {
			return null;
		}
		return instance.stringValue(attributeIndex);
	}

	private Integer readInteger(Instance instance, int attributeIndex) {
		if (instance.isMissing(attributeIndex)) {
			return null;
		}
		return (int) Math.round(instance.value(attributeIndex));
	}

	private Long readLong(Instance instance, int attributeIndex) {
		if (instance.isMissing(attributeIndex)) {
			return null;
		}
		return Math.round(instance.value(attributeIndex));
	}

	private Double readDouble(Instance instance, int attributeIndex) {
		if (instance.isMissing(attributeIndex)) {
			return null;
		}
		return instance.value(attributeIndex);
	}
}