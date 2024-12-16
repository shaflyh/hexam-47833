package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hand.demo.api.dto.InvoiceHeaderReportDTO;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.*;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;
import com.hand.demo.infra.mapper.InvoiceApplyLineMapper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.apaas.common.userinfo.domain.UserVO;
import org.hzero.boot.apaas.common.userinfo.infra.feign.IamRemoteService;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.redis.RedisHelper;
import org.hzero.core.redis.RedisQueueHelper;
import org.hzero.mybatis.domian.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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
    private final IamRemoteService iamRemoteService;

    private static final Logger logger = LoggerFactory.getLogger(InvoiceApplyHeaderServiceImpl.class);

    @Autowired
    public InvoiceApplyHeaderServiceImpl(InvoiceApplyHeaderRepository headerRepository,
                                         InvoiceApplyHeaderMapper invoiceApplyHeaderMapper,
                                         InvoiceApplyLineRepository lineRepository,
                                         InvoiceApplyLineMapper invoiceApplyLineMapper, LovAdapter lovAdapter,
                                         CodeRuleBuilder codeRuleBuilder, RedisHelper redisHelper,
                                         RedisQueueHelper redisQueueHelper, IamRemoteService iamRemoteService) {
        this.headerRepository = headerRepository;
        this.invoiceApplyHeaderMapper = invoiceApplyHeaderMapper;
        this.lineRepository = lineRepository;
        this.invoiceApplyLineMapper = invoiceApplyLineMapper;
        this.lovAdapter = lovAdapter;
        this.codeRuleBuilder = codeRuleBuilder;
        this.redisHelper = redisHelper;
        this.redisQueueHelper = redisQueueHelper;
        this.iamRemoteService = iamRemoteService;
    }

    /**
     * Question 3:
     * Select list method with return Header DTO list with meaning LoV meaning
     * Fuzzy search implemented in the XML mapper
     */
    @Override
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeader) {
        // Check if user is admin
        if (getUserVO().getTenantAdminFlag() == null) {
            // Add filtering if user not admin
            invoiceApplyHeader.setTenantAdminFlag(false);
            invoiceApplyHeader.setTenantId(getUserVO().getTenantId());
            invoiceApplyHeader.setCreatedBy(getUserVO().getId());
        }
        return PageHelper.doPageAndSort(pageRequest, () -> headerRepository.selectList(invoiceApplyHeader));
    }

    @Override
    @ProcessLovValue
    public InvoiceHeaderReportDTO selectListReport(InvoiceHeaderReportDTO reportDTO, Long organizationId) {
        Condition condition = new Condition(InvoiceApplyHeader.class);
        // Filter for Invoice Apply Number range
        if (reportDTO.getInvoiceApplyNumberFrom() != null && reportDTO.getInvoiceApplyNumberTo() != null) {
            condition.and()
                     .andBetween(InvoiceApplyHeader.FIELD_APPLY_HEADER_NUMBER, reportDTO.getInvoiceApplyNumberFrom(),
                             reportDTO.getInvoiceApplyNumberTo());
        }
        // Filter for Submit Time range
        if (reportDTO.getSubmitTimeFrom() != null && reportDTO.getSubmitTimeTo() != null) {
            condition.and().andBetween(InvoiceApplyHeader.FIELD_SUBMIT_TIME, reportDTO.getSubmitTimeFrom(),
                    reportDTO.getSubmitTimeTo());
        }
        // Filter for Invoice Creation Date range
        if (reportDTO.getInvoiceCreationDateFrom() != null && reportDTO.getInvoiceCreationDateTo() != null) {
            condition.and().andBetween(InvoiceApplyHeader.FIELD_CREATION_DATE, reportDTO.getInvoiceCreationDateFrom(),
                    reportDTO.getInvoiceCreationDateTo());
        }
        // Filter for Apply Status (multi)
        if (reportDTO.getApplyStatusList() != null && !reportDTO.getApplyStatusList().isEmpty()) {
            condition.and().andIn(InvoiceApplyHeader.FIELD_APPLY_STATUS, reportDTO.getApplyStatusList());
        }
        // Filter for Invoice Type (single)
        if (reportDTO.getInvoiceType() != null) {
            condition.and().andEqualTo(InvoiceApplyHeader.FIELD_INVOICE_TYPE, reportDTO.getInvoiceType());
        }
        // Set Invoice Headers
        List<InvoiceApplyHeader> headers = headerRepository.selectByCondition(condition);
        // TODO: Make it more efficient (don't use selectAll)
        List<InvoiceApplyLine> invoiceApplyLines = lineRepository.selectAll();
        List<InvoiceApplyHeaderDTO> dtoList = new ArrayList<>();
        for (InvoiceApplyHeader header : headers) {
            InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
            BeanUtils.copyProperties(header, dto);
            List<String> linelist = new ArrayList<>();
            for (InvoiceApplyLine line : invoiceApplyLines) {
                if (line.getApplyHeaderId().equals(header.getApplyHeaderId())) {
                    linelist.add(line.getInvoiceName());
                }
            }
            dto.setInvoiceApplyLineNames(String.join(",", linelist));
            dtoList.add(dto);
        }
        // TODO add meaning in the response
        reportDTO.setInvoiceApplyHeaderList(dtoList);

        // Set Tenant Name
        String tenantName = getUserVO().getTenantName();
        if (tenantName.isEmpty()) {
            throw new CommonException("Tenant name is missing in response");
        }
        reportDTO.setTenantName(tenantName);
        return reportDTO;
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
        // Add user real name to the response
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        headerDTO.setRequester(userDetails.getRealName());

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
    public void saveData(List<InvoiceApplyHeader> invoiceApplyHeaders, Long organizationId) {
        inputValidation(invoiceApplyHeaders, organizationId);
        List<InvoiceApplyHeader> insertList = getNewInvoices(invoiceApplyHeaders);
        List<InvoiceApplyHeader> updateList = getExistingInvoices(invoiceApplyHeaders);
        updateInvoiceHeader(updateList);
        insertInvoiceHeader(insertList);
    }

    @Override
    public void saveDataByImport(List<InvoiceApplyHeader> invoiceApplyHeaders, Long organizationId) {
        inputValidation(invoiceApplyHeaders, organizationId);
        List<InvoiceApplyHeader> insertList = getNewInvoices(invoiceApplyHeaders);
        List<InvoiceApplyHeader> updateList = getExistingInvoices(invoiceApplyHeaders);
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
        invoiceHeaderCalculation(invoiceApplyLines);
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
    public List<InvoiceApplyHeaderDTO> exportData(InvoiceApplyHeader invoiceApplyHeader, Long organizationId) {
        // Fetch headers based on the input filter
        List<InvoiceApplyHeaderDTO> headerDTOList = headerRepository.selectList(invoiceApplyHeader);
        if (headerDTOList == null || headerDTOList.isEmpty()) {
            return Collections.emptyList();
        }
        // Fetch all lines and group them by applyHeaderId for quick lookup
        List<InvoiceApplyLine> invoiceApplyLines = lineRepository.selectAll();
        Map<Long, List<InvoiceApplyLine>> linesGroupedByHeaderId =
                invoiceApplyLines.stream().collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));
        // Assign the grouped lines to each header
        headerDTOList.forEach(header -> {
            List<InvoiceApplyLine> lines =
                    linesGroupedByHeaderId.getOrDefault(header.getApplyHeaderId(), new ArrayList<>());
            header.setInvoiceApplyLineList(lines);
        });
        return headerDTOList;
    }

    private List<InvoiceApplyHeader> getNewInvoices(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        return invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() == null)
                                  .collect(Collectors.toList());
    }

    private List<InvoiceApplyHeader> getExistingInvoices(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        return invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null)
                                  .collect(Collectors.toList());
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
        if (!updateList.isEmpty()) {
            // Check if Invoice Header ID exist in the database
            List<Long> updateIds =
                    updateList.stream().map(InvoiceApplyHeader::getApplyHeaderId).collect(Collectors.toList());
            // TODO: This actually not necessary. No need to create new repository/mapper to query data
            List<Long> existingIds = headerRepository.findExistingIds(updateIds);
            for (Long id : updateIds) {
                if (!existingIds.contains(id)) {
                    throw new CommonException(ErrorCodeConst.INVOICE_NOT_EXIST, id);
                }
            }
            //            Sqls.custom().andIn()
            //            headerRepository.selectByCondition();
            //            headerRepository.updateOptional();
            // Calculate total_amount, exclude_tax_amount, and tax_amount
            // TODO: Move calculation on only invoice line change
            //            List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceHeaderCalculation(updateList);
            logger.info("Updating Invoice Headers: {}", updateList);
            headerRepository.batchUpdateByPrimaryKeySelective(updateList);
        }
    }

    private void updateInvoiceByImport(List<InvoiceApplyHeader> updateList) {
        if (!updateList.isEmpty()) {
            List<InvoiceApplyHeader> updatedHeader = new ArrayList<>();
            for (InvoiceApplyHeader newInvoice : updateList) {
                // Check if invoice header apply number exist in the database
                // TODO: Don't have query inside of loop!
                InvoiceApplyHeader header = headerRepository.selectOne(new InvoiceApplyHeader() {{
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
            // TODO: Move calculation to line invoice
            //             Calculate total_amount, exclude_tax_amount, and tax_amount
            //            List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceHeaderCalculation(updatedHeader);
            // Update invoice header
            headerRepository.batchUpdateByPrimaryKeySelective(updatedHeader);
        }
    }

    private void insertInvoiceHeader(List<InvoiceApplyHeader> insertList) {
        if (!insertList.isEmpty()) {
            // Set invoice header name from Code Rule Builder
            // TODO: Make sure the reset is correct
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

    public void invoiceHeaderCalculation(List<InvoiceApplyLine> applyLines) {
        // Fetch corresponding Invoice Headers and relevant Invoice Lines
        Set<String> headerIds =
                applyLines.stream().map(header -> header.getApplyHeaderId().toString()).collect(Collectors.toSet());
        List<InvoiceApplyHeader> fetchedInvHeaders = headerRepository.selectByIds(String.join(",", headerIds));
        List<InvoiceApplyLine> invoiceApplyLines = lineRepository.selectAll();
        // TODO: Create selectByHeaderIds method to change selectAll
        //         List<InvoiceApplyLine> invoiceApplyLines = lineService.selectByHeaderIds(headerIds);

        // Group Invoice Lines by Header ID for faster lookups
        Map<String, List<InvoiceApplyLine>> linesGroupedByHeader =
                invoiceApplyLines.stream().collect(Collectors.groupingBy(line -> line.getApplyHeaderId().toString()));

        // Calculate the total amount, total exclude tax, and total tax
        for (InvoiceApplyHeader invHeader : fetchedInvHeaders) {
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
        // Update Invoice Header after the calculation
        headerRepository.batchUpdateByPrimaryKeySelective(fetchedInvHeaders);
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

    private UserVO getUserVO() {
        ResponseEntity<String> stringResponse = iamRemoteService.selectSelf();
        ObjectMapper objectMapper = new ObjectMapper();
        // Fix object mapper error
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Set a custom date format that matches the API response
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        objectMapper.setDateFormat(dateFormat);
        UserVO userVO;
        try {
            logger.info(stringResponse.getBody());
            userVO = objectMapper.readValue(stringResponse.getBody(), UserVO.class);
        } catch (JsonProcessingException e) {
            throw new CommonException("Failed to parse response body to UserVO", e);
        } catch (Exception e) {
            throw new CommonException("Unexpected error occurred", e);
        }
        return userVO;
    }
}

