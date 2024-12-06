package com.hand.demo.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceInfoQueueService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceInfoQueueRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Redis Message Queue Table(InvoiceInfoQueue)应用服务
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:47:20
 */
@Service
public class InvoiceInfoQueueServiceImpl implements InvoiceInfoQueueService {
    private final InvoiceInfoQueueRepository invoiceInfoQueueRepository;

    @Autowired
    public InvoiceInfoQueueServiceImpl(InvoiceInfoQueueRepository invoiceInfoQueueRepository) {
        this.invoiceInfoQueueRepository = invoiceInfoQueueRepository;
    }

    @Override
    public Page<InvoiceInfoQueue> selectList(PageRequest pageRequest, InvoiceInfoQueue invoiceInfoQueue) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceInfoQueueRepository.selectList(invoiceInfoQueue));
    }

    @Override
    public void saveData(List<InvoiceInfoQueue> invoiceInfoQueues) {
        List<InvoiceInfoQueue> insertList =
                invoiceInfoQueues.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<InvoiceInfoQueue> updateList =
                invoiceInfoQueues.stream().filter(line -> line.getId() != null).collect(Collectors.toList());
        invoiceInfoQueueRepository.batchInsertSelective(insertList);
        invoiceInfoQueueRepository.batchUpdateByPrimaryKeySelective(updateList);
    }
}

