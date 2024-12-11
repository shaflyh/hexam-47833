package com.hand.demo.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.infra.constant.Constants;
import com.hand.demo.infra.constant.ImportConst;
import io.choerodon.core.exception.CommonException;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  Question 9
 * @author Shafly - 47833
 * @since 2024-12-05 14:47
 */
@ImportService(templateCode = ImportConst.INV_HEADER_TEMP_CODE, sheetName = ImportConst.INV_HEADER_SHEET_NAME)
public class InvoiceHeaderImportService extends BatchImportHandler {

    private final InvoiceApplyHeaderService headerService;
    private final ObjectMapper objectMapper;

    @Autowired
    public InvoiceHeaderImportService(InvoiceApplyHeaderService headerService, ObjectMapper objectMapper) {
        this.headerService = headerService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Boolean doImport(List<String> data) {
        try {
            List<InvoiceApplyHeaderDTO> invoiceApplyHeaderList = new ArrayList<>();
            for (String header : data) {
                InvoiceApplyHeaderDTO invoiceApplyHeader = objectMapper.readValue(header, InvoiceApplyHeaderDTO.class);
                invoiceApplyHeader.setTenantId(Constants.ORGANIZATION_ID);
                invoiceApplyHeaderList.add(invoiceApplyHeader);
            }
            headerService.saveDataByImport(invoiceApplyHeaderList, Constants.ORGANIZATION_ID);
            return true;
        } catch (IOException e) {
            throw new CommonException("Import error");
        }
    }
}