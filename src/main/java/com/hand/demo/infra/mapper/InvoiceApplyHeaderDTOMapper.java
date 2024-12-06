package com.hand.demo.infra.mapper;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

/**
 * @author Shafly - 47833
 * @since 2024-12-03 16:38
 */
public class InvoiceApplyHeaderDTOMapper {
    public static InvoiceApplyHeaderDTO toDTO(InvoiceApplyHeader entity) {
        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();

        // Map fields from entity to DTO
        dto.setApplyHeaderId(entity.getApplyHeaderId());
        dto.setTenantId(entity.getTenantId());
        dto.setApplyHeaderNumber(entity.getApplyHeaderNumber());
        dto.setApplyStatus(entity.getApplyStatus());
        dto.setSubmitTime(entity.getSubmitTime());
        dto.setInvoiceColor(entity.getInvoiceColor());
        dto.setInvoiceType(entity.getInvoiceType());
        dto.setBillToPerson(entity.getBillToPerson());
        dto.setBillToPhone(entity.getBillToPhone());
        dto.setBillToAddress(entity.getBillToAddress());
        dto.setBillToEmail(entity.getBillToEmail());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setExcludeTaxAmount(entity.getExcludeTaxAmount());
        dto.setTaxAmount(entity.getTaxAmount());
        dto.setDelFlag(entity.getDelFlag());
        dto.setRemark(entity.getRemark() != null ? entity.getRemark().toString() : null); // Convert Object to String
        dto.setObjectVersionNumber(entity.getObjectVersionNumber());
        dto.setCreationDate(entity.getCreationDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setLastUpdatedBy(entity.getLastUpdatedBy());
        dto.setLastUpdateDate(entity.getLastUpdateDate());

        return dto;
    }
}