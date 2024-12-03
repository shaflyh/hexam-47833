package com.hand.demo.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author muhammad.shafly@hand-global.com
 * @since 2024-12-03 10:47:03
 */
@Service
public class InvoiceApplyLineServiceImpl implements InvoiceApplyLineService {
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Override
    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));
    }

    @Override
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        List<InvoiceApplyLine> insertList =
                invoiceApplyLines.stream().filter(line -> line.getApplyLineId() == null).collect(Collectors.toList());
        List<InvoiceApplyLine> updateList =
                invoiceApplyLines.stream().filter(line -> line.getApplyLineId() != null).collect(Collectors.toList());
        invoiceApplyLineRepository.batchInsertSelective(insertList);
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
    }
}

