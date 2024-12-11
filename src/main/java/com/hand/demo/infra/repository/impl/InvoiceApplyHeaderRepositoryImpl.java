package com.hand.demo.infra.repository.impl;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.infra.mapper.InvoiceApplyLineMapper;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * (InvoiceApplyHeader)资源库
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:46:31
 */
@Component
public class InvoiceApplyHeaderRepositoryImpl extends BaseRepositoryImpl<InvoiceApplyHeaderDTO>
        implements InvoiceApplyHeaderRepository {
    @Resource
    private InvoiceApplyHeaderMapper invoiceApplyHeaderMapper;
    @Resource
    private InvoiceApplyLineMapper invoiceApplyLineMapper;
    @Resource
    private RedisHelper redisHelper;

    @Override
    public List<InvoiceApplyHeaderDTO> selectList(InvoiceApplyHeaderDTO invoiceApplyHeader) {
        return invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
    }

    @Override
    public InvoiceApplyHeader selectByPrimary(Long applyHeaderId) {
        InvoiceApplyHeaderDTO invoiceApplyHeader = new InvoiceApplyHeaderDTO();
        invoiceApplyHeader.setApplyHeaderId(applyHeaderId);
        List<InvoiceApplyHeaderDTO> invoiceApplyHeaders = invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
        if (invoiceApplyHeaders.isEmpty()) {
            return null;
        }
        return invoiceApplyHeaders.get(0);
    }

    @Override
    public List<Long> findExistingIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return invoiceApplyHeaderMapper.findExistingIds(ids);
    }

    /**
     * Question 6:
     * Invoice Header Delete by updating the delete flag
     * Create deleteById method in mapper to implement this
     */
    @Override
    public void batchDeleteById(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        for (InvoiceApplyHeader invoice : invoiceApplyHeaders) {
            invoiceApplyHeaderMapper.deleteById(invoice.getApplyHeaderId());
        }
    }
}

