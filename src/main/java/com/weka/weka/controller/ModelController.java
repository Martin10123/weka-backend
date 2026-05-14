package com.weka.weka.controller;

import com.weka.weka.contract.response.ModelTrainingResponse;
import com.weka.weka.service.TitanicModelTrainingService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/models")
public class ModelController {
	private final TitanicModelTrainingService titanicModelTrainingService;

	public ModelController(TitanicModelTrainingService titanicModelTrainingService) {
		this.titanicModelTrainingService = titanicModelTrainingService;
	}

	@PostMapping(value = "/train", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ModelTrainingResponse train(@RequestParam("file") MultipartFile file) {
		return titanicModelTrainingService.train(file);
	}
}