package com.hand.demo.api.dto;

import com.hand.demo.infra.constant.LovConst;
import lombok.Getter;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author muhammad.shafly@hand-global.com
 * @since 2024-12-03 14:30
 */
@Getter
@Setter
public class InvoiceApplyHeaderDTO {
    private Long applyHeaderId;
    private Long tenantId;
    private String applyHeaderNumber;
    @LovValue(lovCode = LovConst.InvoiceHeader.INV_STATUS)
    private String applyStatus;
    private Date submitTime;
    @LovValue(lovCode = LovConst.InvoiceHeader.INV_COLOR)
    private String invoiceColor;
    @LovValue(lovCode = LovConst.InvoiceHeader.INV_TYPE)
    private String invoiceType;
    private String billToPerson;
    private String billToPhone;
    private String billToAddress;
    private String billToEmail;
    private BigDecimal totalAmount;
    private BigDecimal excludeTaxAmount;
    private BigDecimal taxAmount;
    private Integer delFlag;
    private String remark;
    private Long objectVersionNumber;
    private Date creationDate;
    private Long createdBy;
    private Long lastUpdatedBy;
    private Date lastUpdateDate;

    // Additional fields for meaning values
    private String applyStatusMeaning;
    private String invoiceColorMeaning;
    private String invoiceTypeMeaning;
}
