package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.ErrorCodeConst;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
    private InvoiceApplyLineRepository lineRepository;

    @Autowired
    private InvoiceApplyHeaderRepository headerRepository;

    @Autowired
    private InvoiceApplyHeaderService headerService;

    @Override
    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        return PageHelper.doPageAndSort(pageRequest, () -> lineRepository.selectList(invoiceApplyLine));
    }

    @Override
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        List<InvoiceApplyLine> insertList =
                invoiceApplyLines.stream().filter(line -> line.getApplyLineId() == null).collect(Collectors.toList());
        List<InvoiceApplyLine> updateList =
                invoiceApplyLines.stream().filter(line -> line.getApplyLineId() != null).collect(Collectors.toList());
        updateInvoiceLines(updateList);
        insertInvoiceLines(insertList);
    }

    private void updateInvoiceLines(List<InvoiceApplyLine> updateList) {
        if (!updateList.isEmpty()) {
            // Update invoice lines validation
            updateInvoiceLineValidation(updateList);
            // Invoice lines calculation
            List<InvoiceApplyLine> invoiceApplyLines = invoiceLineCalculation(updateList);
            // Update invoice lines
            lineRepository.batchUpdateByPrimaryKeySelective(invoiceApplyLines);
        }
    }

    private void insertInvoiceLines(List<InvoiceApplyLine> insertList) {
        if (!insertList.isEmpty()) {
            // Invoice lines calculation
            List<InvoiceApplyLine> invoiceApplyLines = invoiceLineCalculation(insertList);
            // Insert new invoice lines
            lineRepository.batchInsertSelective(invoiceApplyLines);
        }
    }

    private void updateInvoiceLineValidation(List<InvoiceApplyLine> updateList) {
        // Check if invoice apply header exist in database and not deleted
        for (InvoiceApplyLine line : updateList) {
            InvoiceApplyHeader invoiceApplyHeader = new InvoiceApplyHeader();
            invoiceApplyHeader.setApplyHeaderId(line.getApplyHeaderId());
            InvoiceApplyHeader header = headerRepository.selectOne(invoiceApplyHeader);
            if (header == null) {
                throw new CommonException(ErrorCodeConst.INVOICE_NOT_EXIST, line.getApplyHeaderId());
            } else if (header.getDelFlag() == 1) {
                throw new CommonException(ErrorCodeConst.INVOICE_DELETED, line.getApplyHeaderId());
            }
        }
    }

    private List<InvoiceApplyLine> invoiceLineCalculation(List<InvoiceApplyLine> invoiceApplyLines) {
        // exclude_tax_amount, tax_amount, and total_amount calculation
        for (InvoiceApplyLine line : invoiceApplyLines) {
            // Fetch data from input
            BigDecimal unitPrice = line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal quantity = line.getQuantity() != null ? line.getQuantity() : BigDecimal.ZERO;
            BigDecimal taxRate = line.getTaxRate() != null ? line.getTaxRate() : BigDecimal.ZERO;
            BigDecimal taxRatePercent = taxRate.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            // Calculation
            BigDecimal totalAmount = unitPrice.multiply(quantity); // total_amount = unit_price * quantity
            BigDecimal taxAmount = totalAmount.multiply(taxRatePercent); // tax_amount = total_amount * tax_rate
            BigDecimal excludeTaxAmount = totalAmount.subtract(taxAmount); // = total_amount – tax_amount
            // Set the data
            line.setTotalAmount(totalAmount);
            line.setTaxAmount(taxAmount);
            line.setExcludeTaxAmount(excludeTaxAmount);
        }
        return invoiceApplyLines;
    }
}

