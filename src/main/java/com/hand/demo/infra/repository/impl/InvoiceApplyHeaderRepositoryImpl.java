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
    public InvoiceApplyHeaderDTO selectByPrimary(Long applyHeaderId) {
        InvoiceApplyHeader invoiceApplyHeader = new InvoiceApplyHeader();
        invoiceApplyHeader.setApplyHeaderId(applyHeaderId);
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
        if (invoiceApplyHeaders.size() == 0) {
            return null;
        }
        InvoiceApplyHeaderDTO headerDTO = InvoiceApplyHeaderDTOMapper.toDTO(invoiceApplyHeaders.get(0));
        // Get the invoice lines into the invoice header detail
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineMapper.selectLinesByHeaderId(applyHeaderId);
        headerDTO.setInvoiceApplyLineList(invoiceApplyLines);
        // Save invoice header detail in Redis
        try {
            // Serialize the DTO to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String dtoJson = objectMapper.writeValueAsString(headerDTO);
            // Save to Redis
            String redisKey = "hexam-47833:invoice-header:" + headerDTO.getApplyHeaderId();
            redisHelper.hshPut(redisKey, String.valueOf(headerDTO.getApplyHeaderId()), dtoJson);
            redisHelper.setExpire(redisKey, 10, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new CommonException("Failed to serialize DTO: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CommonException("Redis operation failed: " + e.getMessage(), e);
        }
        return headerDTO;
    }

    @Override
    public List<Long> findExistingIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return invoiceApplyHeaderMapper.findExistingIds(ids);
    }

    @Override
    public void batchDeleteById(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        for (InvoiceApplyHeader invoice : invoiceApplyHeaders) {
            invoiceApplyHeaderMapper.deleteById(invoice.getApplyHeaderId());
        }
    }
}

