package com.hand.demo.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.InvoiceInfoQueueService;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceInfoQueueRepository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * Redis Message Queue Table(InvoiceInfoQueue)表控制层
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:47:20
 */

@RestController("invoiceInfoQueueController.v1")
@RequestMapping("/v1/{organizationId}/invoice-info-queues")
public class InvoiceInfoQueueController extends BaseController {

    private final InvoiceInfoQueueRepository invoiceInfoQueueRepository;
    private final InvoiceInfoQueueService invoiceInfoQueueService;

    @Autowired
    public InvoiceInfoQueueController(InvoiceInfoQueueRepository invoiceInfoQueueRepository,
                                      InvoiceInfoQueueService invoiceInfoQueueService) {
        this.invoiceInfoQueueRepository = invoiceInfoQueueRepository;
        this.invoiceInfoQueueService = invoiceInfoQueueService;
    }

    @ApiOperation(value = "Redis Message Queue Table列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<InvoiceInfoQueue>> list(InvoiceInfoQueue invoiceInfoQueue,
                                                       @PathVariable Long organizationId,
                                                       @ApiIgnore @SortDefault(value = InvoiceInfoQueue.FIELD_ID,
                                                               direction = Sort.Direction.DESC)
                                                       PageRequest pageRequest) {
        Page<InvoiceInfoQueue> list = invoiceInfoQueueService.selectList(pageRequest, invoiceInfoQueue);
        return Results.success(list);
    }

    @ApiOperation(value = "Redis Message Queue Table明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}/detail")
    public ResponseEntity<InvoiceInfoQueue> detail(@PathVariable Long id) {
        InvoiceInfoQueue invoiceInfoQueue = invoiceInfoQueueRepository.selectByPrimary(id);
        return Results.success(invoiceInfoQueue);
    }

    @ApiOperation(value = "创建或更新Redis Message Queue Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvoiceInfoQueue>> save(@PathVariable Long organizationId,
                                                       @RequestBody List<InvoiceInfoQueue> invoiceInfoQueues) {
        validObject(invoiceInfoQueues);
        SecurityTokenHelper.validTokenIgnoreInsert(invoiceInfoQueues);
        invoiceInfoQueues.forEach(item -> item.setTenantId(organizationId));
        invoiceInfoQueueService.saveData(invoiceInfoQueues);
        return Results.success(invoiceInfoQueues);
    }

    @ApiOperation(value = "删除Redis Message Queue Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<InvoiceInfoQueue> invoiceInfoQueues) {
        SecurityTokenHelper.validToken(invoiceInfoQueues);
        invoiceInfoQueueRepository.batchDeleteByPrimaryKey(invoiceInfoQueues);
        return Results.success();
    }

}

