package com.hand.demo.app.service;

import com.hand.demo.api.dto.HfleUploadConfigDTO;

import java.util.List;

/**
 * ExampleService
 */
public interface ExampleService {

    List<HfleUploadConfigDTO> uploadConfig(HfleUploadConfigDTO hfleUploadConfigDTO);

}
