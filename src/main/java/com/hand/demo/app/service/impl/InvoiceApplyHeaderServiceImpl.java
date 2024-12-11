package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.*;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;
import com.hand.demo.infra.mapper.InvoiceApplyLineMapper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.redis.RedisHelper;
import org.hzero.core.redis.RedisQueueHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:46:31
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {

    private final InvoiceApplyHeaderRepository headerRepository;
    private final InvoiceApplyHeaderMapper invoiceApplyHeaderMapper;
    private final InvoiceApplyLineRepository lineRepository;
    private final InvoiceApplyLineMapper invoiceApplyLineMapper;
    private final LovAdapter lovAdapter;
    private final CodeRuleBuilder codeRuleBuilder;
    private final RedisHelper redisHelper;
    private final RedisQueueHelper redisQueueHelper;

    private static final Logger logger = LoggerFactory.getLogger(InvoiceApplyHeaderServiceImpl.class);

    @Autowired
    public InvoiceApplyHeaderServiceImpl(InvoiceApplyHeaderRepository headerRepository,
                                         InvoiceApplyHeaderMapper invoiceApplyHeaderMapper,
                                         InvoiceApplyLineRepository lineRepository,
                                         InvoiceApplyLineMapper invoiceApplyLineMapper, LovAdapter lovAdapter,
                                         CodeRuleBuilder codeRuleBuilder, RedisHelper redisHelper,
                                         RedisQueueHelper redisQueueHelper) {
        this.headerRepository = headerRepository;
        this.invoiceApplyHeaderMapper = invoiceApplyHeaderMapper;
        this.lineRepository = lineRepository;
        this.invoiceApplyLineMapper = invoiceApplyLineMapper;
        this.lovAdapter = lovAdapter;
        this.codeRuleBuilder = codeRuleBuilder;
        this.redisHelper = redisHelper;
        this.redisQueueHelper = redisQueueHelper;
    }

    @Override
    public Page<InvoiceApplyHeader> selectList(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeader) {
        return PageHelper.doPageAndSort(pageRequest, () -> headerRepository.selectList(invoiceApplyHeader));
    }

    /**
     * Question 3:
     * Select list method with return Header DTO list with meaning LoV meaning
     * Fuzzy search implemented in the XML mapper
     */
    @Override
    public Page<InvoiceApplyHeaderDTO> selectListWithMeaning(PageRequest pageRequest,
                                                             InvoiceApplyHeaderDTO invoiceApplyHeader,
                                                             Long organizationId) {
        return PageHelper.doPageAndSort(pageRequest, () -> headerRepository.selectList(invoiceApplyHeader));
    }

    /**
     * Question 4:
     *
     * @return InvoiceApplyHeaderDTO with the Invoice meanings and Invoice Line list for selected header id
     * Cache Invoice Header detail to Redis before return
     */
    @Override
    public InvoiceApplyHeaderDTO selectDetail(Long applyHeaderId) {
        InvoiceApplyHeaderDTO invoiceApplyHeader = new InvoiceApplyHeaderDTO();
        invoiceApplyHeader.setApplyHeaderId(applyHeaderId);
        List<InvoiceApplyHeaderDTO> invoiceApplyHeaders = invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
        if (invoiceApplyHeaders.isEmpty()) {
            return null;
        }
        InvoiceApplyHeaderDTO headerDTO = invoiceApplyHeaders.get(0);
        // Get the invoice lines into the invoice header detail
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineMapper.selectLinesByHeaderId(applyHeaderId);
        headerDTO.setInvoiceApplyLineList(invoiceApplyLines);
        // Save invoice header detail in Redis
        saveHeaderRedis(headerDTO);
        return headerDTO;
    }

    /**
     * Question 5:
     * Invoice Header save and update with:
     * - Input validation with apply_status, invoice_color, invoice_type validation
     * - Calculate total_amount, exclude_tax_amount, and tax_amount on Invoice Header update
     * - Code rule generation for Header number on Invoice Header insert
     */
    @Override
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders, Long organizationId) {
        inputValidation(invoiceApplyHeaders, organizationId);
        List<InvoiceApplyHeaderDTO> insertList = getNewInvoices(invoiceApplyHeaders);
        List<InvoiceApplyHeaderDTO> updateList = getExistingInvoices(invoiceApplyHeaders);
        updateInvoiceHeader(updateList);
        insertInvoiceHeader(insertList);
    }

    @Override
    public void saveDataByImport(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders, Long organizationId) {
        inputValidation(invoiceApplyHeaders, organizationId);
        List<InvoiceApplyHeaderDTO> insertList = getNewInvoices(invoiceApplyHeaders);
        List<InvoiceApplyHeaderDTO> updateList = getExistingInvoices(invoiceApplyHeaders);
        updateInvoiceByImport(updateList);
        insertInvoiceHeader(insertList);
    }

    /**
     * Schedules task method to process invoice data and saves it to a Redis queue.
     * Converts data from the database to JSON format and pushes it to the specified Redis key.
     */
    @Override
    public void invoiceSchedulingTask(String delFlag, String applyStatus, String invoiceColor, String invoiceType) {
        // Get a list of Invoice Header with selected parameter
        InvoiceApplyHeaderDTO headerDTO = new InvoiceApplyHeaderDTO();
        headerDTO.setDelFlag(Integer.parseInt(delFlag));
        headerDTO.setApplyStatus(applyStatus);
        headerDTO.setInvoiceColor(invoiceColor);
        headerDTO.setInvoiceType(invoiceType);
        List<InvoiceApplyHeaderDTO> invoiceApplyHeaders = headerRepository.selectList(headerDTO);
        if (invoiceApplyHeaders.isEmpty()) {
            logger.info("InvoiceApplyHeaders is empty for scheduling task");
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        // Convert each InvoiceApplyHeader to JSON string and push to Redis
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
        List<InvoiceApplyHeaderDTO> headerList = new ArrayList<>();
        for (InvoiceApplyLine line : invoiceApplyLines) {
            InvoiceApplyHeaderDTO header = headerRepository.selectByPrimaryKey(line.getApplyHeaderId());
            headerList.add(header);
        }
        updateInvoiceHeader(headerList);
    }

    /**
     * Question 9:
     * Export Invoice Header
     *
     * @return List InvoiceApplyHeaderDTO
     * Parse the meaning by adding @ProcessLovValue
     */
    @Override
    @ProcessLovValue
    public List<InvoiceApplyHeaderDTO> exportData(InvoiceApplyHeaderDTO invoiceApplyHeader, Long organizationId) {
        return headerRepository.selectList(invoiceApplyHeader);
    }

    private List<InvoiceApplyHeaderDTO> getNewInvoices(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
        return invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() == null)
                                  .collect(Collectors.toList());
    }

    private List<InvoiceApplyHeaderDTO> getExistingInvoices(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
        return invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null)
                                  .collect(Collectors.toList());
    }

    private void inputValidation(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders, Long organizationId) {
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

    private void updateInvoiceHeader(List<InvoiceApplyHeaderDTO> updateList) {
        if (!updateList.isEmpty()) {
            // Check if Invoice Header ID exist in the database
            List<Long> updateIds =
                    updateList.stream().map(InvoiceApplyHeader::getApplyHeaderId).collect(Collectors.toList());
            List<Long> existingIds = headerRepository.findExistingIds(updateIds);
            for (Long id : updateIds) {
                if (!existingIds.contains(id)) {
                    throw new CommonException(ErrorCodeConst.INVOICE_NOT_EXIST, id);
                }
            }
            // Calculate total_amount, exclude_tax_amount, and tax_amount
            List<InvoiceApplyHeaderDTO> invoiceApplyHeaders = invoiceHeaderCalculation(updateList);
            headerRepository.batchUpdateByPrimaryKeySelective(invoiceApplyHeaders);
        }
    }

    private void updateInvoiceByImport(List<InvoiceApplyHeaderDTO> updateList) {
        if (!updateList.isEmpty()) {
            List<InvoiceApplyHeaderDTO> updatedHeader = new ArrayList<>();
            for (InvoiceApplyHeaderDTO newInvoice : updateList) {
                // Check if invoice header apply number exist in the database
                // TODO: Don't have query inside of loop!
                InvoiceApplyHeaderDTO header = headerRepository.selectOne(new InvoiceApplyHeaderDTO() {{
                    setApplyHeaderNumber(newInvoice.getApplyHeaderNumber());
                }});
                if (header == null) {
                    throw new CommonException(ErrorCodeConst.INVOICE_NOT_EXIST, newInvoice.getApplyHeaderNumber());
                }
                // Set the Invoice Header based on import value
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
            // Calculate total_amount, exclude_tax_amount, and tax_amount
            List<InvoiceApplyHeaderDTO> invoiceApplyHeaders = invoiceHeaderCalculation(updatedHeader);
            // Update invoice header
            headerRepository.batchUpdateByPrimaryKeySelective(invoiceApplyHeaders);
        }
    }

    private void insertInvoiceHeader(List<InvoiceApplyHeaderDTO> insertList) {
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

    public List<InvoiceApplyHeaderDTO> invoiceHeaderCalculation(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
        // Fetch corresponding Invoice Headers and relevant Invoice Lines
        Set<String> headerIds = invoiceApplyHeaders.stream().map(header -> header.getApplyHeaderId().toString())
                                                   .collect(Collectors.toSet());
        List<InvoiceApplyHeaderDTO> fetchedInvHeaders = headerRepository.selectByIds(String.join(",", headerIds));
        List<InvoiceApplyLine> invoiceApplyLines = lineRepository.selectAll();
        // TODO: Create selectByHeaderIds method to change selectAll
        // List<InvoiceApplyLine> invoiceApplyLines = lineService.selectByHeaderIds(headerIds);

        // Group Invoice Lines by Header ID for faster lookups
        Map<String, List<InvoiceApplyLine>> linesGroupedByHeader =
                invoiceApplyLines.stream().collect(Collectors.groupingBy(line -> line.getApplyHeaderId().toString()));

        // Calculate the total amount, total exclude tax, and total tax
        for (InvoiceApplyHeaderDTO invHeader : fetchedInvHeaders) {
            List<InvoiceApplyLine> linesForHeader =
                    linesGroupedByHeader.getOrDefault(invHeader.getApplyHeaderId().toString(), Collections.emptyList());
            // Total calculation
            BigDecimal totalAmount = linesForHeader.stream().map(InvoiceApplyLine::getTotalAmount)
                                                   .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalExcludeTax = linesForHeader.stream().map(InvoiceApplyLine::getExcludeTaxAmount)
                                                       .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalTax = linesForHeader.stream().map(InvoiceApplyLine::getTaxAmount)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
            // Update totals for each Invoice Header
            invHeader.setTotalAmount(totalAmount);
            invHeader.setExcludeTaxAmount(totalExcludeTax);
            invHeader.setTaxAmount(totalTax);
        }
        return fetchedInvHeaders;
    }

    private void saveHeaderRedis(InvoiceApplyHeaderDTO headerDTO) {
        try {
            // Serialize the DTO to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String dtoJson = objectMapper.writeValueAsString(headerDTO);
            // Save to Redis
            logger.info("Saving Invoice Header detail to Redis...");
            String redisKey = RedisKeyConst.REDIS_HEADER_DETAIL_KEY + headerDTO.getApplyHeaderId();
            redisHelper.hshPut(redisKey, String.valueOf(headerDTO.getApplyHeaderId()), dtoJson);
            redisHelper.setExpire(redisKey, 10, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new CommonException("Failed to serialize DTO: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CommonException("Redis operation failed: " + e.getMessage(), e);
        }
    }
}

