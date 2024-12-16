package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
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
 * Question 10
 * Import Invoice Header and save or update by saveDataByImport method
 * @author Shafly - 47833
 * @since 2024-12-06 08:36
 */
@ImportService(templateCode = ImportConst.INV_LINE_TEMP_CODE, sheetName = ImportConst.INV_LINE_SHEET_NAME)
public class InvoiceLineImportService extends BatchImportHandler {

    private final InvoiceApplyLineService lineService;
    private final ObjectMapper objectMapper;

    @Autowired
    public InvoiceLineImportService(ObjectMapper objectMapper, InvoiceApplyLineService lineService) {
        this.objectMapper = objectMapper;
        this.lineService = lineService;
    }

    @Override
    public Boolean doImport(List<String> data) {
        try {
            List<InvoiceApplyLine> invoiceApplyLineList = new ArrayList<>();
            for (String line : data) {
                InvoiceApplyLine invoiceApplyLine = objectMapper.readValue(line, InvoiceApplyLine.class);
                invoiceApplyLine.setTenantId(Constants.ORGANIZATION_ID);
                invoiceApplyLineList.add(invoiceApplyLine);
            }
            lineService.saveDataByImport(invoiceApplyLineList, Constants.ORGANIZATION_ID);
            return true;
        } catch (IOException e) {
            throw new CommonException("Import Invoice error");
        }
    }
}
