package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.InvoiceInfoQueue;

import java.util.List;

/**
 * Redis Message Queue Table(InvoiceInfoQueue)应用服务
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:47:20
 */
public interface InvoiceInfoQueueMapper extends BaseMapper<InvoiceInfoQueue> {
    /**
     * 基础查询
     *
     * @param invoiceInfoQueue 查询条件
     * @return 返回值
     */
    List<InvoiceInfoQueue> selectList(InvoiceInfoQueue invoiceInfoQueue);
}

