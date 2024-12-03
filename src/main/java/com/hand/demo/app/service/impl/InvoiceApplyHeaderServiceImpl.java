package com.hand.demo.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author muhammad.shafly@hand-global.com
 * @since 2024-12-03 10:46:31
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Override
    public Page<InvoiceApplyHeader> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));
    }

    @Override
    public void saveData(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        List<InvoiceApplyHeader> insertList =
                invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() == null)
                        .collect(Collectors.toList());
        List<InvoiceApplyHeader> updateList =
                invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null)
                        .collect(Collectors.toList());
        invoiceApplyHeaderRepository.batchInsertSelective(insertList);
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);
    }
}

