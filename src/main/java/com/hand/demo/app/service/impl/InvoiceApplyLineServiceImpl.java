package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.ErrorCodeConst;
import com.hand.demo.infra.mapper.InvoiceApplyLineMapper;
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
 * @author Shafly - 47833
 * @since 2024-12-03 10:47:03
 */
@Service
public class InvoiceApplyLineServiceImpl implements InvoiceApplyLineService {
    private final InvoiceApplyLineRepository lineRepository;
    private final InvoiceApplyHeaderRepository headerRepository;
    private final InvoiceApplyHeaderService headerService;
    private final InvoiceApplyLineMapper lineMapper;

    @Autowired
    public InvoiceApplyLineServiceImpl(InvoiceApplyLineRepository lineRepository,
                                       InvoiceApplyHeaderRepository headerRepository,
                                       InvoiceApplyHeaderService headerService, InvoiceApplyLineMapper lineMapper) {
        this.lineRepository = lineRepository;
        this.headerRepository = headerRepository;
        this.headerService = headerService;
        this.lineMapper = lineMapper;
    }

    @Override
    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        return PageHelper.doPageAndSort(pageRequest, () -> lineRepository.selectList(invoiceApplyLine));
    }

    /**
     * Question 7:
     * Invoice Line save and update
     * - updateInvoiceLines method for update
     * - updateInvoiceLineValidation() for update validation
     * - invoiceLineCalculation() for exclude_tax_amount, tax_amount, and total_amount calculation
     * - insertInvoiceLines method for save
     */
    @Override
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        processSaveData(invoiceApplyLines, false, null);
    }

    @Override
    public void saveDataByImport(List<InvoiceApplyLine> invoiceApplyLines, Long organizationId) {
        processSaveData(invoiceApplyLines, true, organizationId);
    }

    private void processSaveData(List<InvoiceApplyLine> invoiceApplyLines, boolean isImport, Long organizationId) {
        List<InvoiceApplyLine> insertList =
                invoiceApplyLines.stream().filter(line -> line.getApplyLineId() == null).collect(Collectors.toList());
        List<InvoiceApplyLine> updateList =
                invoiceApplyLines.stream().filter(line -> line.getApplyLineId() != null).collect(Collectors.toList());

        if (isImport) {
            updateInvoiceLinesByImport(updateList);
        } else {
            updateInvoiceLines(updateList);
        }
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

    private void updateInvoiceLinesByImport(List<InvoiceApplyLine> updateList) {
        if (!updateList.isEmpty()) {
            // Update invoice lines validation
            updateInvoiceLineValidation(updateList);
            List<InvoiceApplyLine> newInvoiceLines = new ArrayList<>();
            for (InvoiceApplyLine line : updateList) {
                // Check if invoice header apply number exist in database
                // TODO: Fix this
                InvoiceApplyLine newInvoiceLine = lineRepository.selectOne(new InvoiceApplyLine() {{
                    setApplyLineId(line.getApplyLineId());
                }});
                if (newInvoiceLine == null) {
                    throw new CommonException(ErrorCodeConst.INVOICE_NOT_EXIST, line.getApplyLineId());
                }
                // Update the new value
                newInvoiceLine.setApplyHeaderId(line.getApplyHeaderId());
                newInvoiceLine.setInvoiceName(line.getInvoiceName());
                newInvoiceLine.setContentName(line.getContentName());
                newInvoiceLine.setTaxClassificationNumber(line.getTaxClassificationNumber());
                newInvoiceLine.setUnitPrice(line.getUnitPrice());
                newInvoiceLine.setQuantity(line.getQuantity());
                newInvoiceLine.setTaxRate(line.getTaxRate());
                newInvoiceLine.setRemark(line.getRemark());
                newInvoiceLines.add(newInvoiceLine);
            }
            // Invoice lines calculation
            List<InvoiceApplyLine> calculatedInvoiceLines = invoiceLineCalculation(newInvoiceLines);
            // Update the Invoice Lines
            lineRepository.batchUpdateByPrimaryKeySelective(calculatedInvoiceLines);
            // Update the Invoice Headers
            headerService.updateHeaderByInvoiceLines(calculatedInvoiceLines);
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
        // Check if invoice apply header exist in the database and not deleted
        for (InvoiceApplyLine line : updateList) {
            InvoiceApplyHeaderDTO invoiceApplyHeader = new InvoiceApplyHeaderDTO();
            invoiceApplyHeader.setApplyHeaderId(line.getApplyHeaderId());
            InvoiceApplyHeaderDTO header = headerRepository.selectOne(invoiceApplyHeader);
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

    @Override
    public List<InvoiceApplyHeaderDTO> exportData(InvoiceApplyLine invoiceApplyLine, Long organizationId) {
        return headerService.exportData(new InvoiceApplyHeaderDTO(), organizationId);
    }
}

