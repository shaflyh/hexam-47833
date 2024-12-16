package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.HfleUploadConfigDTO;
import com.hand.demo.app.service.ExampleService;
import com.hand.demo.infra.mapper.ExampleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ExampleServiceImpl
 */
@Service
public class ExampleServiceImpl implements ExampleService {

    private final ExampleMapper exampleMapper;

    @Autowired
    public ExampleServiceImpl(ExampleMapper exampleMapper) {
        this.exampleMapper = exampleMapper;
    }

    @Override
    public List<HfleUploadConfigDTO> uploadConfig(HfleUploadConfigDTO hfleUploadConfigDTO) {
        return exampleMapper.selectListUploadConfig(hfleUploadConfigDTO);
    }
}
