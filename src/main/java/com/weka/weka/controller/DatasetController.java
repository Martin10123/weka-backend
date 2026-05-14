package com.weka.weka.controller;

import com.weka.weka.contract.response.DatasetNormalizationResponse;
import com.weka.weka.contract.response.DatasetNormalizationRow;
import com.weka.weka.contract.response.DatasetPreviewResponse;
import com.weka.weka.contract.response.DatasetUploadResponse;
import com.weka.weka.contract.response.DatasetPreviewRow;
import com.weka.weka.contract.response.DatasetValidationResponse;
import com.weka.weka.domain.titanic.TitanicDataset;
import com.weka.weka.service.TitanicDatasetReader;
import com.weka.weka.service.TitanicDatasetNormalizer;
import com.weka.weka.service.TitanicDatasetSchemaValidator;
import java.util.List;
import java.util.Locale;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/datasets")
public class DatasetController {

	private final TitanicDatasetReader titanicDatasetReader;
	private final TitanicDatasetNormalizer titanicDatasetNormalizer;
	private final TitanicDatasetSchemaValidator titanicDatasetSchemaValidator;

	public DatasetController(TitanicDatasetReader titanicDatasetReader, TitanicDatasetNormalizer titanicDatasetNormalizer, TitanicDatasetSchemaValidator titanicDatasetSchemaValidator) {
		this.titanicDatasetReader = titanicDatasetReader;
		this.titanicDatasetNormalizer = titanicDatasetNormalizer;
		this.titanicDatasetSchemaValidator = titanicDatasetSchemaValidator;
	}

	@PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DatasetValidationResponse validate(@RequestParam("file") MultipartFile file) {
		var validation = titanicDatasetSchemaValidator.validate(file);
		return new DatasetValidationResponse(
				validation.valid(),
				validation.detectedFormat(),
				validation.message(),
				validation.expectedColumns(),
				validation.actualColumns()
		);
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DatasetUploadResponse upload(@RequestParam("file") MultipartFile file) {
		TitanicDataset dataset = titanicDatasetReader.read(file);
		return new DatasetUploadResponse(
				dataset.sourceFileName(),
				detectFormat(dataset.sourceFileName()),
				dataset.rows().size()
		);
	}

	@GetMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DatasetPreviewResponse preview(
			@RequestParam("file") MultipartFile file,
			@RequestParam(name = "limit", defaultValue = "5") int limit
	) {
		TitanicDataset dataset = titanicDatasetReader.read(file);
		int safeLimit = Math.max(1, Math.min(limit, 20));
		List<DatasetPreviewRow> previewRows = dataset.rows().stream()
				.limit(safeLimit)
				.map(row -> new DatasetPreviewRow(
						row.passengerId(),
						row.survived(),
						row.pclass(),
						row.name(),
						row.sex(),
						row.age(),
						row.sibSp(),
						row.parch(),
						row.ticket(),
						row.fare(),
						row.cabin(),
						row.embarked()
				))
				.toList();

		return new DatasetPreviewResponse(
				dataset.sourceFileName(),
				detectFormat(dataset.sourceFileName()),
				dataset.rows().size(),
				safeLimit,
				previewRows
		);
	}

	@PostMapping(value = "/normalize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DatasetNormalizationResponse normalize(@RequestParam("file") MultipartFile file) {
		TitanicDataset dataset = titanicDatasetReader.read(file);
		var normalizedDataset = titanicDatasetNormalizer.normalize(dataset);
		List<DatasetNormalizationRow> normalizedRows = normalizedDataset.rows().stream()
				.map(row -> new DatasetNormalizationRow(
						row.passengerId(),
						row.survived(),
						row.passengerClass().name(),
						row.sex().toCsvValue(),
						row.age(),
						row.travelingAlone(),
						row.embarked() == null ? null : row.embarked().getCsvValue()
				))
				.toList();

		return new DatasetNormalizationResponse(
				dataset.sourceFileName(),
				detectFormat(dataset.sourceFileName()),
				dataset.rows().size(),
				normalizedRows.size(),
				normalizedDataset.discardedRows(),
				normalizedRows
		);
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
}