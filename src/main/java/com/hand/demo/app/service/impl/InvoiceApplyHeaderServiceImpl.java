package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.infra.constant.CodeRuleConst;
import com.hand.demo.infra.constant.Constants;
import com.hand.demo.infra.constant.ErrorCodeConst;
import com.hand.demo.infra.constant.LovConst;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderDTOMapper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.domain.PageInfo;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.redis.RedisQueueHelper;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author muhammad.shafly@hand-global.com
 * @since 2024-12-03 10:46:31
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {

    private final InvoiceApplyHeaderRepository headerRepository;
    private final LovAdapter lovAdapter;
    private final CodeRuleBuilder codeRuleBuilder;
    private final RedisQueueHelper redisQueueHelper;

    @Autowired
    public InvoiceApplyHeaderServiceImpl(InvoiceApplyHeaderRepository headerRepository, LovAdapter lovAdapter,
                                         CodeRuleBuilder codeRuleBuilder, RedisQueueHelper redisQueueHelper) {
        this.headerRepository = headerRepository;
        this.lovAdapter = lovAdapter;
        this.codeRuleBuilder = codeRuleBuilder;
        this.redisQueueHelper = redisQueueHelper;
    }

    @Override
    public Page<InvoiceApplyHeader> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {
        return PageHelper.doPageAndSort(pageRequest, () -> headerRepository.selectList(invoiceApplyHeader));
    }

    @Override
    public Page<InvoiceApplyHeaderDTO> selectListWithMeaning(PageRequest pageRequest,
                                                             InvoiceApplyHeader invoiceApplyHeader,
                                                             Long organizationId) {
        // Fetch paginated data
        Page<InvoiceApplyHeader> invoiceApplyHeaderPage =
                PageHelper.doPageAndSort(pageRequest, () -> headerRepository.selectList(invoiceApplyHeader));
        // Transform entities to DTOs
        List<InvoiceApplyHeaderDTO> headerDTOList = new ArrayList<>();
        for (InvoiceApplyHeader invoice : invoiceApplyHeaderPage) {
            InvoiceApplyHeaderDTO dto = InvoiceApplyHeaderDTOMapper.toDTO(invoice);
            headerDTOList.add(dto);
        }
        // Create PageInfo object from PageRequest
        PageInfo pageInfo = new PageInfo(pageRequest.getPage(), pageRequest.getSize());
        return new Page<>(headerDTOList, pageInfo, invoiceApplyHeaderPage.getTotalElements());
    }

    @Override
    public void saveData(List<InvoiceApplyHeader> invoiceApplyHeaders, Long organizationId) {
        inputValidation(invoiceApplyHeaders, organizationId);
        List<InvoiceApplyHeader> insertList =
                invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() == null)
                        .collect(Collectors.toList());
        List<InvoiceApplyHeader> updateList =
                invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null)
                        .collect(Collectors.toList());
        // Validate and update existing records
        updateInvoiceHeader(updateList);
        // Insert new records
        insertInvoiceHeader(insertList);
    }

    @Override
    public void saveDataByImport(List<InvoiceApplyHeader> invoiceApplyHeaders, Long organizationId) {
        inputValidation(invoiceApplyHeaders, organizationId);
        // Check for invoice header number (instead of header id)
        List<InvoiceApplyHeader> insertList =
                invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderNumber() == null)
                        .collect(Collectors.toList());
        List<InvoiceApplyHeader> updateList =
                invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderNumber() != null)
                        .collect(Collectors.toList());
        // Validate and update existing records
        updateInvoiceByImport(updateList);
        // Insert new records
        insertInvoiceHeader(insertList);
    }

    /**
     * Schedules task method to process invoice data and saves it to a Redis queue.
     * Converts data from the database to JSON format and pushes it to the specified Redis key.
     */
    @Override
    public void invoiceSchedulingTask(String delFlag, String applyStatus, String invoiceColor, String invoiceType) {
        List<InvoiceApplyHeader> invoiceApplyHeaders = headerRepository.selectList(new InvoiceApplyHeader() {{
            setDelFlag(Integer.parseInt(delFlag));
            setApplyStatus(applyStatus);
            setInvoiceColor(invoiceColor);
            setInvoiceType(invoiceType);
        }});
        if (invoiceApplyHeaders.isEmpty()) {
            System.out.println("InvoiceApplyHeaders is empty for scheduling task");
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        // Convert each InvoiceApplyHeader to JSON string and collect into a List
        try {
            // Convert list to JSON string
            String jsonString = objectMapper.writeValueAsString(invoiceApplyHeaders);
            // Save to Redis Message Queue
            String redisKey = "invoiceInfo_" + Constants.EMPLOYEE_ID;
            redisQueueHelper.push(redisKey, jsonString);
        } catch (JsonProcessingException e) {
            throw new CommonException("Error converting list to JSON");
        }
    }

    @Override
    public void updateHeaderByInvoiceLines(List<InvoiceApplyLine> invoiceApplyLines) {
        List<InvoiceApplyHeader> headerList = new ArrayList<>();
        for (InvoiceApplyLine line : invoiceApplyLines) {
            InvoiceApplyHeader header = headerRepository.selectByPrimaryKey(line.getApplyHeaderId());
            headerList.add(header);
        }
        updateInvoiceHeader(headerList);
    }

    @Override
    public List<InvoiceApplyHeaderDTO> exportData(InvoiceApplyHeader invoiceApplyHeader, Long organizationId) {
        List<InvoiceApplyHeader> headerList = headerRepository.selectList(invoiceApplyHeader);

        // Get list of value for invoice
        List<LovValueDTO> invStatusList = lovAdapter.queryLovValue(LovConst.InvoiceHeader.INV_STATUS, organizationId);
        List<LovValueDTO> invColorList = lovAdapter.queryLovValue(LovConst.InvoiceHeader.INV_COLOR, organizationId);
        List<LovValueDTO> invTypeList = lovAdapter.queryLovValue(LovConst.InvoiceHeader.INV_TYPE, organizationId);

        List<InvoiceApplyHeaderDTO> headerDTOList = new ArrayList<>();
        for (InvoiceApplyHeader header : headerList) {
            InvoiceApplyHeaderDTO dto = InvoiceApplyHeaderDTOMapper.toDTO(header);
            // Set invoice apply status
            for (LovValueDTO lovDto : invStatusList) {
                if (dto.getApplyStatus().equals(lovDto.getValue())) {
                    dto.setApplyStatusMeaning(lovDto.getMeaning());
                    break;
                }
            }
            // Set invoice color
            for (LovValueDTO lovDto : invColorList) {
                if (dto.getInvoiceColor().equals(lovDto.getValue())) {
                    dto.setInvoiceColorMeaning(lovDto.getMeaning());
                    break;
                }
            }
            // Set invoice type
            for (LovValueDTO lovDto : invTypeList) {
                if (dto.getInvoiceType().equals(lovDto.getValue())) {
                    dto.setInvoiceTypeMeaning(lovDto.getMeaning());
                    break;
                }
            }
            headerDTOList.add(dto);
        }
        return headerDTOList;
    }

    private void inputValidation(List<InvoiceApplyHeader> invoiceApplyHeaders, Long organizationId) {
        // apply_status, invoice_color, and invoice_type validation
        List<LovValueDTO> invStatusList = lovAdapter.queryLovValue(LovConst.InvoiceHeader.INV_STATUS, organizationId);
        List<LovValueDTO> invColorList = lovAdapter.queryLovValue(LovConst.InvoiceHeader.INV_COLOR, organizationId);
        List<LovValueDTO> invTypeList = lovAdapter.queryLovValue(LovConst.InvoiceHeader.INV_TYPE, organizationId);

        Set<String> validStatuses = invStatusList.stream().map(LovValueDTO::getValue).collect(Collectors.toSet());
        Set<String> validColors = invColorList.stream().map(LovValueDTO::getValue).collect(Collectors.toSet());
        Set<String> validTypes = invTypeList.stream().map(LovValueDTO::getValue).collect(Collectors.toSet());

        for (InvoiceApplyHeader invoice : invoiceApplyHeaders) {
            invoice.setSubmitTime(Date.from(Instant.now()));
            if (!validStatuses.contains(invoice.getApplyStatus())) {
                throw new CommonException(ErrorCodeConst.INVOICE_VALUE_INVALID, "apply status");
            }
            if (!validColors.contains(invoice.getInvoiceColor())) {
                throw new CommonException(ErrorCodeConst.INVOICE_VALUE_INVALID, "invoice color");
            }
            if (!validTypes.contains(invoice.getInvoiceType())) {
                throw new CommonException(ErrorCodeConst.INVOICE_VALUE_INVALID, "invoice type");
            }
        }
    }

    private void updateInvoiceHeader(List<InvoiceApplyHeader> updateList) {
        // Check if invoice header apply id exist in database
        if (!updateList.isEmpty()) {
            List<Long> updateIds =
                    updateList.stream().map(InvoiceApplyHeader::getApplyHeaderId).collect(Collectors.toList());
            List<Long> existingIds = headerRepository.findExistingIds(updateIds);
            for (Long id : updateIds) {
                if (!existingIds.contains(id)) {
                    throw new CommonException(ErrorCodeConst.INVOICE_NOT_EXIST, id);
                }
            }
        } else {
            return;
        }
        // Total amount calculation
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceHeaderCalculation(updateList);
        // Update invoice header
        headerRepository.batchUpdateByPrimaryKeySelective(invoiceApplyHeaders);
    }

    private void updateInvoiceByImport(List<InvoiceApplyHeader> updateList) {
        if (!updateList.isEmpty()) {
            List<InvoiceApplyHeader> updatedHeader = new ArrayList<>();
            for (InvoiceApplyHeader newInvoice : updateList) {
                // Check if invoice header apply number exist in database
                InvoiceApplyHeader header = headerRepository.selectOne(new InvoiceApplyHeader() {{
                    setApplyHeaderNumber(newInvoice.getApplyHeaderNumber());
                }});
                if (header == null) {
                    throw new CommonException(ErrorCodeConst.INVOICE_NOT_EXIST, newInvoice.getApplyHeaderNumber());
                }
                // Update the new value
                header.setApplyStatus(newInvoice.getApplyStatus());
                header.setInvoiceColor(newInvoice.getInvoiceColor());
                header.setInvoiceType(newInvoice.getInvoiceType());

                header.setBillToAddress(newInvoice.getBillToAddress());
                header.setBillToEmail(newInvoice.getBillToEmail());
                header.setBillToPerson(newInvoice.getBillToPerson());
                header.setBillToPhone(newInvoice.getBillToPhone());

                header.setSubmitTime(newInvoice.getSubmitTime());
                header.setRemark(newInvoice.getRemark());
                updatedHeader.add(header);
            }
            // Total amount calculation
            List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceHeaderCalculation(updatedHeader);
            // Update invoice header
            headerRepository.batchUpdateByPrimaryKeySelective(invoiceApplyHeaders);
        }
    }

    private void insertInvoiceHeader(List<InvoiceApplyHeader> insertList) {
        if (!insertList.isEmpty()) {
            // Set invoice header name from Code Rule Builder
            for (InvoiceApplyHeader invHeader : insertList) {
                Map<String, String> codeBuilderMap = new HashMap<>();
                String invoiceCode = codeRuleBuilder.generateCode(CodeRuleConst.INV_HEADER_NUMBER, codeBuilderMap);
                invHeader.setApplyHeaderNumber(invoiceCode);
                invHeader.setDelFlag(0);
            }
            // Insert new invoice header
            headerRepository.batchInsertSelective(insertList);
        }
    }

    public List<InvoiceApplyHeader> invoiceHeaderCalculation(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        for (InvoiceApplyHeader invHeader : invoiceApplyHeaders) {
            InvoiceApplyHeaderDTO detailHeader = headerRepository.selectByPrimary(invHeader.getApplyHeaderId());
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalExcludeTax = BigDecimal.ZERO;
            BigDecimal totalTax = BigDecimal.ZERO;
            for (InvoiceApplyLine line : detailHeader.getInvoiceApplyLineList()) {
                totalAmount = totalAmount.add(line.getTotalAmount());
                totalExcludeTax = totalExcludeTax.add(line.getExcludeTaxAmount());
                totalTax = totalTax.add(line.getTaxAmount());
            }
            invHeader.setTotalAmount(totalAmount);
            invHeader.setTaxAmount(totalTax);
            invHeader.setExcludeTaxAmount(totalExcludeTax);
        }
        return invoiceApplyHeaders;
    }
}

