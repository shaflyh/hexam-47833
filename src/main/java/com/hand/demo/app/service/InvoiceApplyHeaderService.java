package com.hand.demo.app.service;

import com.hand.demo.api.dto.InvoiceHeaderReportDTO;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:46:31
 */
public interface InvoiceApplyHeaderService {

    /**
     * 查询数据
     *
     * @param pageRequest         分页参数
     * @param invoiceApplyHeaders 查询条件
     * @return 返回值
     */
    Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeaders);

    /**
     * 查询数据
     *
     * @return InvoiceApplyHeaderDTO Page
     */
    InvoiceHeaderReportDTO selectListReport(InvoiceHeaderReportDTO headerRequestReportDTO, Long organizationId);

    /**
     * Select Invoice Header detail by Header ID
     *
     * @param applyHeaderId applyHeaderId
     * @return InvoiceApplyHeaderDTO
     */
    InvoiceApplyHeaderDTO selectDetail(Long applyHeaderId);

    /**
     * Save and update invoice header
     *
     * @param invoiceApplyHeaders 数据
     * @param organizationId      Tenant id
     */
    void saveData(List<InvoiceApplyHeader> invoiceApplyHeaders, Long organizationId);

    /**
     * Update Header By Invoice Lines
     *
     * @param invoiceApplyLines Invoice apply lines
     */
    void updateHeaderByInvoiceLines(List<InvoiceApplyLine> invoiceApplyLines);

    /**
     * Export invoice header data
     *
     * @param invoiceApplyHeader Invoice Apply Header
     */
    List<InvoiceApplyHeaderDTO> exportData(InvoiceApplyHeader invoiceApplyHeader, Long organizationId);

    /**
     * Save and update invoice header by import
     *
     * @param invoiceApplyHeaders Invoice Apply Header
     * @param organizationId      Tenant id
     */
    void saveDataByImport(List<InvoiceApplyHeader> invoiceApplyHeaders, Long organizationId);

    /**
     * Invoice Scheduling Task
     */
    void invoiceSchedulingTask(String delFlag, String applyStatus, String invoiceColor, String invoiceType);
}

