package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
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
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * (InvoiceApplyLine)表控制层
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:47:03
 */

@RestController("invoiceApplyLineController.v1")
@RequestMapping("/v1/{organizationId}/invoice-apply-lines")
public class InvoiceApplyLineController extends BaseController {

    private final InvoiceApplyLineRepository invoiceApplyLineRepository;
    private final InvoiceApplyLineService invoiceApplyLineService;
    private final InvoiceApplyHeaderService invoiceApplyHeaderService;

    @Autowired
    public InvoiceApplyLineController(InvoiceApplyLineRepository invoiceApplyLineRepository,
                                      InvoiceApplyLineService invoiceApplyLineService,
                                      InvoiceApplyHeaderService invoiceApplyHeaderService) {
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
        this.invoiceApplyLineService = invoiceApplyLineService;
        this.invoiceApplyHeaderService = invoiceApplyHeaderService;
    }

    @ApiOperation(value = "列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<InvoiceApplyLine>> list(InvoiceApplyLine invoiceApplyLine,
                                                       @PathVariable Long organizationId, @ApiIgnore
                                                       @SortDefault(value = InvoiceApplyLine.FIELD_APPLY_LINE_ID,
                                                               direction = Sort.Direction.DESC)
                                                       PageRequest pageRequest) {
        Page<InvoiceApplyLine> list = invoiceApplyLineService.selectList(pageRequest, invoiceApplyLine);
        return Results.success(list);
    }

    @ApiOperation(value = "明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{applyLineId}/detail")
    public ResponseEntity<InvoiceApplyLine> detail(@PathVariable Long organizationId, @PathVariable Long applyLineId) {
        InvoiceApplyLine invoiceApplyLine = invoiceApplyLineRepository.selectByPrimary(applyLineId);
        return Results.success(invoiceApplyLine);
    }

    @ApiOperation(value = "Create or Update")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvoiceApplyLine>> save(@PathVariable Long organizationId,
                                                       @RequestBody List<InvoiceApplyLine> invoiceApplyLines) {
        validObject(invoiceApplyLines);
        SecurityTokenHelper.validTokenIgnoreInsert(invoiceApplyLines);
        invoiceApplyLines.forEach(item -> item.setTenantId(organizationId));
        invoiceApplyLineService.saveData(invoiceApplyLines);
        invoiceApplyHeaderService.updateHeaderByInvoiceLines(invoiceApplyLines);
        return Results.success(invoiceApplyLines);
    }

    /**
     * Question 8:
     * Invoice Line Delete
     * - Update the Invoice Header after delete
     */
    @ApiOperation(value = "Delete")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@PathVariable Long organizationId,
                                    @RequestBody List<InvoiceApplyLine> invoiceApplyLines) {
        SecurityTokenHelper.validToken(invoiceApplyLines);
        // TODO: Move it to service!
        // Getting the Invoice Lines first before deleting the data (needed to get the the Invoice Header ID)
        List<InvoiceApplyLine> fetchedInvoiceApplyLines = new ArrayList<>();
        for (InvoiceApplyLine line : invoiceApplyLines) {
            fetchedInvoiceApplyLines.add(invoiceApplyLineRepository.selectOne(line));
        }
        // Delete Invoice Line
        invoiceApplyLineRepository.batchDeleteByPrimaryKey(invoiceApplyLines);
        // Update the total in Invoice Header by Invoice Line
        invoiceApplyHeaderService.updateHeaderByInvoiceLines(fetchedInvoiceApplyLines);
        return Results.success();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "Export")
    @GetMapping("/export")
    @ExcelExport(value = InvoiceApplyHeaderDTO.class)
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<List<InvoiceApplyHeaderDTO>> export(@PathVariable Long organizationId,
                                                              InvoiceApplyLine invoiceApplyLine,
                                                              ExportParam exportParam, HttpServletResponse response) {
        return Results.success(invoiceApplyLineService.exportData(invoiceApplyLine, organizationId));
    }

}

