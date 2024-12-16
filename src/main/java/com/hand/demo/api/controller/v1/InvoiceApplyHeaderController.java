package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.InvoiceHeaderReportDTO;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.export.annotation.ExcelExport;
import org.hzero.export.vo.ExportParam;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * (InvoiceApplyHeader)表控制层
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:46:31
 */

@RestController("invoiceApplyHeaderController.v1")
@RequestMapping("/v1/{organizationId}/invoice-apply-headers")
public class InvoiceApplyHeaderController extends BaseController {

    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private final InvoiceApplyHeaderService invoiceApplyHeaderService;

    @Autowired
    public InvoiceApplyHeaderController(InvoiceApplyHeaderRepository invoiceApplyHeaderRepository,
                                        InvoiceApplyHeaderService invoiceApplyHeaderService) {
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.invoiceApplyHeaderService = invoiceApplyHeaderService;
    }

    @ApiOperation(value = "List")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @GetMapping
    public ResponseEntity<Page<InvoiceApplyHeaderDTO>> list(InvoiceApplyHeaderDTO invoiceApplyHeader,
                                                            @PathVariable Long organizationId, @ApiIgnore @SortDefault(
                    value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID, direction = Sort.Direction.DESC)
                                                            PageRequest pageRequest) {
        Page<InvoiceApplyHeaderDTO> list = invoiceApplyHeaderService.selectList(pageRequest, invoiceApplyHeader);
        return Results.success(list);
    }

    @ApiOperation(value = "List Report")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @GetMapping("/list-report")
    public ResponseEntity<InvoiceHeaderReportDTO> listReport(InvoiceHeaderReportDTO invoiceApplyHeader,
                                                             @PathVariable Long organizationId) {
        InvoiceHeaderReportDTO list = invoiceApplyHeaderService.selectListReport(invoiceApplyHeader, organizationId);
        return Results.success(list);
    }

    @ApiOperation(value = "Detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @GetMapping("/{applyHeaderId}/detail")
    public ResponseEntity<InvoiceApplyHeaderDTO> detail(@PathVariable Long applyHeaderId,
                                                        @PathVariable Long organizationId) {
        return Results.success(invoiceApplyHeaderService.selectDetail(applyHeaderId));
    }

    @ApiOperation(value = "Create or Update")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvoiceApplyHeader>> save(@PathVariable Long organizationId,
                                                         @RequestBody List<InvoiceApplyHeader> invoiceApplyHeaders) {
        validObject(invoiceApplyHeaders);
        SecurityTokenHelper.validTokenIgnoreInsert(invoiceApplyHeaders);
        invoiceApplyHeaders.forEach(item -> item.setTenantId(organizationId));
        invoiceApplyHeaderService.saveData(invoiceApplyHeaders, organizationId);
        return Results.success(invoiceApplyHeaders);
    }

    @ApiOperation(value = "Delete")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@PathVariable Long organizationId,
                                    @RequestBody List<InvoiceApplyHeader> invoiceApplyHeaders) {
        SecurityTokenHelper.validToken(invoiceApplyHeaders);
        // TODO: Need to make sure first if data exist in the database
        // use updateOptional to update it in service
        invoiceApplyHeaderRepository.batchDeleteById(invoiceApplyHeaders);
        // TODO: Fix token... (where do we get the token beside takes it from console?)
        return Results.success();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "Export")
    @GetMapping("/export")
    @ExcelExport(value = InvoiceApplyHeaderDTO.class)
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<List<InvoiceApplyHeaderDTO>> export(@PathVariable Long organizationId,
                                                              InvoiceApplyHeaderDTO invoiceApplyHeader,
                                                              ExportParam exportParam, HttpServletResponse response) {
        return Results.success(invoiceApplyHeaderService.exportData(invoiceApplyHeader, organizationId));
    }
}

