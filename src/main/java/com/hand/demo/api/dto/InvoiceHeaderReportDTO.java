package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Shafly - 47833
 * @since 2024-12-13 13:26
 */
@Getter
@Setter
public class InvoiceHeaderReportDTO {
    // Variables for date range filtering
    private String invoiceCreationDateFrom;

    private String invoiceCreationDateTo;

    private String submitTimeFrom;

    private String submitTimeTo;

    // Filters for Invoice Apply Number
    private String invoiceApplyNumberFrom;

    private String invoiceApplyNumberTo;

    // Filters for Apply Status
    private List<String> applyStatusList;

    // Filter for Invoice Type
    private String invoiceType;

    private String tenantName;

    private List<InvoiceApplyHeaderDTO> invoiceApplyHeaderList;
}
