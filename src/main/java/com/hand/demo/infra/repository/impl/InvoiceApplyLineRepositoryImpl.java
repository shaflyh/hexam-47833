package com.hand.demo.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.mapper.InvoiceApplyLineMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * (InvoiceApplyLine)资源库
 *
 * @author muhammad.shafly@hand-global.com
 * @since 2024-12-03 10:47:02
 */
@Component
public class InvoiceApplyLineRepositoryImpl extends BaseRepositoryImpl<InvoiceApplyLine>
        implements InvoiceApplyLineRepository {
    @Resource
    private InvoiceApplyLineMapper invoiceApplyLineMapper;

    @Override
    public List<InvoiceApplyLine> selectList(InvoiceApplyLine invoiceApplyLine) {
        return invoiceApplyLineMapper.selectList(invoiceApplyLine);
    }

    @Override
    public InvoiceApplyLine selectByPrimary(Long applyLineId) {
        InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
        invoiceApplyLine.setApplyLineId(applyLineId);
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineMapper.selectList(invoiceApplyLine);
        if (invoiceApplyLines.size() == 0) {
            return null;
        }
        return invoiceApplyLines.get(0);
    }

}

