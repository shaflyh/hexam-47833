package com.hand.demo.app.service;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyLine;

import java.util.List;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author muhammad.shafly@hand-global.com
 * @since 2024-12-03 10:47:03
 */
public interface InvoiceApplyLineService {

    /**
     * 查询数据
     *
     * @param pageRequest       分页参数
     * @param invoiceApplyLines 查询条件
     * @return 返回值
     */
    Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLines);

    /**
     * 保存数据
     *
     * @param invoiceApplyLines 数据
     */
    void saveData(List<InvoiceApplyLine> invoiceApplyLines);

    /**
     * Export Invoice Lines data
     *
     * @param invoiceApplyLine Invoice Apply Line
     */
    List<InvoiceApplyHeaderDTO> exportData(InvoiceApplyLine invoiceApplyLine, Long organizationId);

    /**
     * Save and update invoice header by import
     *
     * @param invoiceApplyLines   Invoice apply header
     * @param organizationId      Tenant id
     */
    void saveDataByImport(List<InvoiceApplyLine> invoiceApplyLines, Long organizationId);
}

