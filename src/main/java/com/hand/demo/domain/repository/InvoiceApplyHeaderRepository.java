package com.hand.demo.domain.repository;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)资源库
 *
 * @author muhammad.shafly@hand-global.com
 * @since 2024-12-03 10:46:31
 */
public interface InvoiceApplyHeaderRepository extends BaseRepository<InvoiceApplyHeader> {
    /**
     * 查询
     *
     * @param invoiceApplyHeader 查询条件
     * @return 返回值
     */
    List<InvoiceApplyHeader> selectList(InvoiceApplyHeader invoiceApplyHeader);

    /**
     * 根据主键查询（可关联表）
     *
     * @param applyHeaderId 主键
     * @return 返回值
     */
    InvoiceApplyHeaderDTO selectByPrimary(Long applyHeaderId);

    /**
     * Check existing invoices id
     *
     * @param ids List id
     * @return List id
     */
    List<Long> findExistingIds(List<Long> ids);

    /**
     * Delete invoice header
     *
     * @param invoiceApplyHeaders invoice header list
     */
    void batchDeleteById(List<InvoiceApplyHeader> invoiceApplyHeaders);
}
