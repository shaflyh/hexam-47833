package com.hand.demo.infra.mapper;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:46:30
 */
public interface InvoiceApplyHeaderMapper extends BaseMapper<InvoiceApplyHeader> {
    /**
     * 基础查询
     *
     * @param invoiceApplyHeader 查询条件
     * @return 返回值
     */
    List<InvoiceApplyHeaderDTO> selectList(InvoiceApplyHeader invoiceApplyHeader);

    /**
     * Query existing IDs
     *
     * @param ids List of IDs to check
     * @return List of existing IDs
     */
    List<Long> findExistingIds(List<Long> ids);

    /**
     * Delete invoice header by Header ID
     *
     * @param id Apply header id
     */
    void deleteById(Long id);
}

