package com.weka.weka.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TitanicDatasetSchemaValidator {
	private static final List<String> EXPECTED_COLUMNS = List.of(
			"PassengerId",
			"Survived",
			"Pclass",
			"Name",
			"Sex",
			"Age",
			"SibSp",
			"Parch",
			"Ticket",
			"Fare",
			"Cabin",
			"Embarked"
	);
	private static final Pattern ARFF_DATA_MARKER = Pattern.compile("(?im)^@data\\s*$");

	public DatasetSchemaValidationResult validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return DatasetSchemaValidationResult.invalid("unknown", "Dataset file is required", EXPECTED_COLUMNS, List.of());
		}

		String fileName = file.getOriginalFilename();
		String lowerCaseName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
		try {
			if (lowerCaseName.endsWith(".arff") || looksLikeArff(file)) {
				return validateArff(file);
			}
			return validateCsv(file);
		} catch (IOException exception) {
			return DatasetSchemaValidationResult.invalid(detectFormat(fileName), "Unable to read dataset file", EXPECTED_COLUMNS, List.of());
		}
	}

	private DatasetSchemaValidationResult validateCsv(MultipartFile file) throws IOException {
		try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
				CSVParser parser = CSVFormat.DEFAULT.builder()
						.setHeader()
						.setSkipHeaderRecord(true)
						.setTrim(true)
						.build()
						.parse(reader)) {

			List<String> actualColumns = parser.getHeaderNames();
			if (!normalizeColumns(actualColumns).equals(normalizeColumns(EXPECTED_COLUMNS))) {
				return DatasetSchemaValidationResult.invalid("csv", "CSV header does not match Titanic schema", EXPECTED_COLUMNS, actualColumns);
			}

			CSVRecord firstRecord = parser.iterator().hasNext() ? parser.iterator().next() : null;
			if (firstRecord == null) {
				return DatasetSchemaValidationResult.invalid("csv", "CSV file has no data rows", EXPECTED_COLUMNS, actualColumns);
			}

			return DatasetSchemaValidationResult.valid("csv", "CSV schema is valid", EXPECTED_COLUMNS, actualColumns);
		}
	}

	private DatasetSchemaValidationResult validateArff(MultipartFile file) throws IOException {
		String content = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		String lowerContent = content.toLowerCase(Locale.ROOT);
		if (!lowerContent.contains("@relation") || !lowerContent.contains("@attribute") || !lowerContent.contains("@data")) {
			return DatasetSchemaValidationResult.invalid("arff", "ARFF file is missing relation, attribute, or data sections", EXPECTED_COLUMNS, List.of());
		}

		String dataSection = extractDataSection(content);
		if (dataSection == null || dataSection.isBlank()) {
			return DatasetSchemaValidationResult.invalid("arff", "ARFF file has no data rows", EXPECTED_COLUMNS, List.of());
		}

		String firstLine = dataSection.lines()
				.map(String::trim)
				.filter(line -> !line.isBlank() && !line.startsWith("%"))
				.findFirst()
				.orElse("");
		if (firstLine.equalsIgnoreCase(String.join(",", EXPECTED_COLUMNS))) {
			return DatasetSchemaValidationResult.invalid("arff", "ARFF data section contains a CSV header line instead of rows", EXPECTED_COLUMNS, List.of(firstLine));
		}

		List<String> actualColumns = readArffAttributeNames(content);
		if (!actualColumns.isEmpty() && actualColumns.size() == EXPECTED_COLUMNS.size()) {
			return DatasetSchemaValidationResult.valid("arff", "ARFF schema is valid", EXPECTED_COLUMNS, actualColumns);
		}

		return DatasetSchemaValidationResult.valid("arff", "ARFF file can be processed", EXPECTED_COLUMNS, actualColumns);
	}

	private String extractDataSection(String content) {
		int dataIndex = ARFF_DATA_MARKER.matcher(content).find() ? content.toLowerCase(Locale.ROOT).lastIndexOf("@data") : -1;
		if (dataIndex < 0) {
			return null;
		}
		return content.substring(dataIndex + 5).trim();
	}

	private List<String> readArffAttributeNames(String content) {
		List<String> attributeNames = new ArrayList<>();
		for (String line : content.split("\\R")) {
			String trimmedLine = line.trim();
			if (trimmedLine.isEmpty() || trimmedLine.startsWith("%")) {
				continue;
			}
			if (trimmedLine.toLowerCase(Locale.ROOT).startsWith("@attribute")) {
				String[] parts = trimmedLine.split("\\s+", 3);
				if (parts.length >= 2) {
					attributeNames.add(parts[1].replace("'", "").replace("\"", ""));
				}
			}
		}
		return attributeNames;
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

	private List<String> normalizeColumns(List<String> columns) {
		return columns.stream().map(column -> column.trim().toLowerCase(Locale.ROOT)).toList();
	}

	private String detectFormat(String fileName) {
		if (fileName == null) {
			return "unknown";
		}
		String lowerCaseName = fileName.toLowerCase(Locale.ROOT);
		if (lowerCaseName.endsWith(".arff")) {
			return "arff";
		}
		if (lowerCaseName.endsWith(".csv")) {
			return "csv";
		}
		return "unknown";
	}

	public record DatasetSchemaValidationResult(
			boolean valid,
			String detectedFormat,
			String message,
			List<String> expectedColumns,
			List<String> actualColumns
	) {
		public static DatasetSchemaValidationResult valid(String detectedFormat, String message, List<String> expectedColumns, List<String> actualColumns) {
			return new DatasetSchemaValidationResult(true, detectedFormat, message, expectedColumns, actualColumns);
		}

		public static DatasetSchemaValidationResult invalid(String detectedFormat, String message, List<String> expectedColumns, List<String> actualColumns) {
			return new DatasetSchemaValidationResult(false, detectedFormat, message, expectedColumns, actualColumns);
		}
	}
}