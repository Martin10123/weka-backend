package com.weka.weka.controller;

import com.weka.weka.contract.request.WhatIfSimulationRequest;
import com.weka.weka.contract.response.WhatIfSimulationResponse;
import com.weka.weka.service.TitanicWhatIfService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/what-if")
public class WhatIfController {
	private final TitanicWhatIfService titanicWhatIfService;

	public WhatIfController(TitanicWhatIfService titanicWhatIfService) {
		this.titanicWhatIfService = titanicWhatIfService;
	}

	@PostMapping
	public WhatIfSimulationResponse simulate(@RequestBody WhatIfSimulationRequest request) {
		return titanicWhatIfService.simulate(request);
	}
}