package com.hand.demo.app.service;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author muhammad.shafly@hand-global.com
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
    Page<InvoiceApplyHeader> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeaders);

    /**
     * 查询数据
     *
     * @param pageRequest         分页参数
     * @param invoiceApplyHeaders 查询条件
     * @param organizationId Tenant id
     * @return InvoiceApplyHeaderDTO Page
     */
    Page<InvoiceApplyHeaderDTO> selectListWithMeaning(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeaders,
                                                      Long organizationId);

    /**
     * Save and update invoice header
     *
     * @param invoiceApplyHeaders 数据
     */
    void saveData(List<InvoiceApplyHeader> invoiceApplyHeaders, Long organizationId);

    /**
     * Update Header By Invoice Lines
     *
     * @param invoiceApplyLines Invoice apply lines
     */
    void updateHeaderByInvoiceLines(List<InvoiceApplyLine> invoiceApplyLines);
}

