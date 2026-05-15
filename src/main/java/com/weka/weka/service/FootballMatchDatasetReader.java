package com.weka.weka.service;

import com.weka.weka.domain.football.FootballMatchDataset;
import com.weka.weka.domain.football.FootballMatchRow;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

@Service
public class FootballMatchDatasetReader {
	private static final List<String> FOOTBALL_COLUMNS = List.of(
			"Division",
			"HomeElo",
			"AwayElo",
			"Form3Home",
			"Form3Away",
			"Form5Home",
			"Form5Away",
			"OddHome",
			"OddDraw",
			"OddAway",
			"FTResult"
	);

	public FootballMatchDataset read(MultipartFile file) {
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
			throw new IllegalStateException("Unable to read football dataset", exception);
		}
	}

	private FootballMatchDataset readCsv(MultipartFile file) throws IOException {
		try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
				CSVParser parser = CSVFormat.DEFAULT.builder()
						.setHeader()
						.setSkipHeaderRecord(true)
						.setTrim(true)
						.build()
						.parse(reader)) {

			List<FootballMatchRow> rows = new ArrayList<>();
			for (CSVRecord record : parser) {
				rows.add(toRow(record));
			}

			return new FootballMatchDataset(file.getOriginalFilename(), rows);
		}
	}

	private FootballMatchDataset readArff(MultipartFile file) throws IOException {
		Path tempFile = Files.createTempFile("football-upload-", ".arff");
		try {
			file.transferTo(tempFile);
			ArffLoader loader = new ArffLoader();
			loader.setFile(tempFile.toFile());
			try {
				Instances instances = loader.getDataSet();
				if (instances.classIndex() < 0 && instances.numAttributes() > 0) {
					instances.setClassIndex(instances.numAttributes() - 1);
				}

				List<FootballMatchRow> rows = new ArrayList<>();
				for (int index = 0; index < instances.numInstances(); index++) {
					rows.add(toRow(instances.instance(index), instances));
				}

				return new FootballMatchDataset(file.getOriginalFilename(), rows);
			} catch (IOException arffException) {
				return readArffAsCsvFallback(file);
			}
		} finally {
			Files.deleteIfExists(tempFile);
		}
	}

	private FootballMatchDataset readArffAsCsvFallback(MultipartFile file) throws IOException {
		String content = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		String dataSection = extractDataSection(content);
		if (dataSection == null || dataSection.isBlank()) {
			throw new IOException("ARFF file has no readable data section");
		}

		String csvContent = String.join(",", FOOTBALL_COLUMNS) + System.lineSeparator() + dataSection.lines()
				.map(String::trim)
				.filter(line -> !line.isBlank() && !line.startsWith("%"))
				.filter(line -> !line.equalsIgnoreCase(String.join(",", FOOTBALL_COLUMNS)))
				.reduce((left, right) -> left + System.lineSeparator() + right)
				.orElse("");

		if (csvContent.isBlank()) {
			throw new IOException("ARFF file has no readable data rows");
		}

		try (Reader reader = new InputStreamReader(new java.io.ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
				CSVParser parser = CSVFormat.DEFAULT.builder()
						.setHeader()
						.setSkipHeaderRecord(true)
						.setTrim(true)
						.build()
						.parse(reader)) {

			List<FootballMatchRow> rows = new ArrayList<>();
			for (CSVRecord record : parser) {
				rows.add(toRow(record));
			}

			return new FootballMatchDataset(file.getOriginalFilename(), rows);
		}
	}

	private FootballMatchRow toRow(CSVRecord record) {
		return new FootballMatchRow(
				getString(record, "Division"),
				parseDouble(record, "HomeElo"),
				parseDouble(record, "AwayElo"),
				parseDouble(record, "Form3Home"),
				parseDouble(record, "Form3Away"),
				parseDouble(record, "Form5Home"),
				parseDouble(record, "Form5Away"),
				parseDouble(record, "OddHome"),
				parseDouble(record, "OddDraw"),
				parseDouble(record, "OddAway"),
				normalizeResult(getString(record, "FTResult"))
		);
	}

	private FootballMatchRow toRow(Instance instance, Instances instances) {
		return new FootballMatchRow(
				readString(instance, instances.attribute("Division")),
				readDouble(instance, instances.attribute("HomeElo")),
				readDouble(instance, instances.attribute("AwayElo")),
				readDouble(instance, instances.attribute("Form3Home")),
				readDouble(instance, instances.attribute("Form3Away")),
				readDouble(instance, instances.attribute("Form5Home")),
				readDouble(instance, instances.attribute("Form5Away")),
				readDouble(instance, instances.attribute("OddHome")),
				readDouble(instance, instances.attribute("OddDraw")),
				readDouble(instance, instances.attribute("OddAway")),
				normalizeResult(readString(instance, instances.attribute("FTResult")))
		);
	}

	private String extractDataSection(String content) {
		String lowerContent = content.toLowerCase(Locale.ROOT);
		int dataIndex = lowerContent.lastIndexOf("@data");
		if (dataIndex < 0) {
			return null;
		}
		return content.substring(dataIndex + 5).trim();
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
		return value.trim();
	}

	private Double parseDouble(CSVRecord record, String column) {
		String value = getString(record, column);
		return value == null ? null : Double.valueOf(value);
	}

	private String readString(Instance instance, weka.core.Attribute attribute) {
		if (attribute == null || instance.isMissing(attribute)) {
			return null;
		}
		return instance.stringValue(attribute);
	}

	private Double readDouble(Instance instance, weka.core.Attribute attribute) {
		if (attribute == null || instance.isMissing(attribute)) {
			return null;
		}
		return instance.value(attribute);
	}

	private String normalizeResult(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		if (normalized.equalsIgnoreCase("H") || normalized.equalsIgnoreCase("Home Win")) {
			return "H";
		}
		if (normalized.equalsIgnoreCase("D") || normalized.equalsIgnoreCase("Draw")) {
			return "D";
		}
		if (normalized.equalsIgnoreCase("A") || normalized.equalsIgnoreCase("Away Win")) {
			return "A";
		}
		return normalized;
	}
}