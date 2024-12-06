package com.hand.demo.app.handler;

import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.HandlerConst;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author Shafly - 47833
 * @since 2024-12-06 09:42
 */
@JobHandler(HandlerConst.INV_HEADER_JOB_HANDLER)
public class InvoiceHeaderJob implements IJobHandler {

    private final InvoiceApplyHeaderService headerService;

    @Autowired
    public InvoiceHeaderJob(InvoiceApplyHeaderService headerService) {
        this.headerService = headerService;
    }

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        // Get parameter from scheduling task
        headerService.invoiceSchedulingTask(map.get("delFlag"), map.get("applyStatus"), map.get("invoiceColor"),
                map.get("invoiceType"));
        return ReturnT.SUCCESS;
    }
}