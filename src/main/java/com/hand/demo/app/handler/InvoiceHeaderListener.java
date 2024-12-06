package com.hand.demo.app.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.app.service.InvoiceInfoQueueService;
import com.hand.demo.domain.entity.HandlerConst;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.infra.constant.Constants;
import org.hzero.core.redis.handler.IQueueHandler;
import org.hzero.core.redis.handler.QueueHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis Consumer for processing messages from the Redis queue.
 * Listens to messages on the HandlerConst.INV_HEADER_QUEUE_HANDLER queue and processes them.
 *
 * @author muhammad.shafly@hand-global.com
 * @since 2024-12-06 10:24
 */
@QueueHandler(HandlerConst.INV_HEADER_QUEUE_HANDLER)
public class InvoiceHeaderListener implements IQueueHandler {

    private final InvoiceInfoQueueService queueService;

    @Autowired
    public InvoiceHeaderListener(InvoiceInfoQueueService queueService) {
        this.queueService = queueService;
    }

    @Override
    public void process(String message) {
        // Parse the message and create an InvoiceInfoQueue object to process it.
        InvoiceInfoQueue queue = new InvoiceInfoQueue();
        queue.setContent(message);
        queue.setEmployeeId(Constants.EMPLOYEE_ID);
        queue.setTenantId(Constants.ORGANIZATION_ID);
        // Save the list of processed queue data
        List<InvoiceInfoQueue> invoiceInfoQueues = new ArrayList<>();
        invoiceInfoQueues.add(queue);
        queueService.saveData(invoiceInfoQueues);
    }
}
