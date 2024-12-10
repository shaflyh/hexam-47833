package com.hand.demo.infra.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderDTOMapper;
import com.hand.demo.infra.mapper.InvoiceApplyLineMapper;
import io.choerodon.core.exception.CommonException;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * (InvoiceApplyHeader)资源库
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:46:31
 */
@Component
public class InvoiceApplyHeaderRepositoryImpl extends BaseRepositoryImpl<InvoiceApplyHeader>
        implements InvoiceApplyHeaderRepository {
    @Resource
    private InvoiceApplyHeaderMapper invoiceApplyHeaderMapper;
    @Resource
    private InvoiceApplyLineMapper invoiceApplyLineMapper;
    @Resource
    private RedisHelper redisHelper;

    @Override
    public List<InvoiceApplyHeader> selectList(InvoiceApplyHeader invoiceApplyHeader) {
        return invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
    }

    @Override
    public InvoiceApplyHeader selectByPrimary(Long applyHeaderId) {
        InvoiceApplyHeader invoiceApplyHeader = new InvoiceApplyHeader();
        invoiceApplyHeader.setApplyHeaderId(applyHeaderId);
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
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

