package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.HfleUploadConfigDTO;
import com.hand.demo.app.service.ExampleService;
import com.hand.demo.config.SwaggerTags;
import com.hand.demo.domain.entity.Example;
import com.hand.demo.domain.repository.ExampleRepository;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API接口
 */
@Api(tags = SwaggerTags.EXAMPLE)
@RestController("exampleController.v1")
@RequestMapping("/v1/example")
public class ExampleController extends BaseController {

    @Autowired
    private ExampleService exampleService;
    @Autowired
    private ExampleRepository exampleRepository;

    @ApiOperation(value = "根据ID获取")
    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @GetMapping("/{id}")
    public ResponseEntity<Example> hello(@PathVariable Long id) {
        return Results.success(exampleRepository.selectByPrimaryKey(id));
    }

    @ApiOperation(value = "Upload Config")
    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @GetMapping("/upload-config")
    public ResponseEntity<List<HfleUploadConfigDTO>> uploadConfig(HfleUploadConfigDTO hfleUploadConfigDTO) {
        return Results.success(exampleService.uploadConfig(hfleUploadConfigDTO));
    }
}