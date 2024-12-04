package com.hand.demo.infra.repository.impl;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderDTOMapper;
import com.hand.demo.infra.mapper.InvoiceApplyLineMapper;
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
 * @author muhammad.shafly@hand-global.com
 * @since 2024-12-03 10:46:31
 */
@Component
public class InvoiceApplyHeaderRepositoryImpl extends BaseRepositoryImpl<InvoiceApplyHeader>
        implements InvoiceApplyHeaderRepository {
    @Resource
    private InvoiceApplyHeaderMapper invoiceApplyHeaderMapper;
    @Resource
    private InvoiceApplyLineMapper invoiceApplyLineMapper;

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
        InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = InvoiceApplyHeaderDTOMapper.toDTO(invoiceApplyHeaders.get(0));
        // Get the invoice lines into the invoice header detail
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineMapper.selectLinesByHeaderId(applyHeaderId);
        invoiceApplyHeaderDTO.setInvoiceApplyLineList(invoiceApplyLines);
        return invoiceApplyHeaderDTO;
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

