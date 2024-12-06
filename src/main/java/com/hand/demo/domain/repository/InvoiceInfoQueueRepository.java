package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.InvoiceInfoQueue;

import java.util.List;

/**
 * Redis Message Queue Table(InvoiceInfoQueue)资源库
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:47:20
 */
public interface InvoiceInfoQueueRepository extends BaseRepository<InvoiceInfoQueue> {
    /**
     * 查询
     *
     * @param invoiceInfoQueue 查询条件
     * @return 返回值
     */
    List<InvoiceInfoQueue> selectList(InvoiceInfoQueue invoiceInfoQueue);

    /**
     * 根据主键查询（可关联表）
     *
     * @param id 主键
     * @return 返回值
     */
    InvoiceInfoQueue selectByPrimary(Long id);
}
