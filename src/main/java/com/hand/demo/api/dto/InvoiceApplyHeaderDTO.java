package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.infra.constant.LovConst;
import lombok.Getter;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author muhammad.shafly@hand-global.com
 * @since 2024-12-03 14:30
 */
@Getter
@Setter
@ExcelSheet(en = "Invoice Apply Header")
public class InvoiceApplyHeaderDTO {
    @ExcelColumn(promptCode = "children", promptKey = "children", child = true)
    private List<InvoiceApplyLine> invoiceApplyLineList;

    @ExcelColumn(en = "Apply Header ID", order = 1)
    private Long applyHeaderId;

    @ExcelColumn(en = "Apply Header Number", order = 2)
    private String applyHeaderNumber;

    @ExcelColumn(en = "Tenant ID", order = 3)
    private Long tenantId;

    @ExcelColumn(en = "Apply Status", order = 4)
    @LovValue(lovCode = LovConst.InvoiceHeader.INV_STATUS)
    private String applyStatus;

    @ExcelColumn(en = "Apply Status Meaning", order = 5)
    private String applyStatusMeaning;

    @ExcelColumn(en = "Invoice Color", order = 6)
    @LovValue(lovCode = LovConst.InvoiceHeader.INV_COLOR)
    private String invoiceColor;

    @ExcelColumn(en = "Invoice Color Meaning", order = 7)
    private String invoiceColorMeaning;

    @ExcelColumn(en = "Invoice Type", order = 8)
    @LovValue(lovCode = LovConst.InvoiceHeader.INV_TYPE)
    private String invoiceType;

    @ExcelColumn(en = "Invoice Type Meaning", order = 9)
    private String invoiceTypeMeaning;

    @ExcelColumn(en = "Bill To Person", order = 10)
    private String billToPerson;

    @ExcelColumn(en = "Bill To Phone", order = 11)
    private String billToPhone;

    @ExcelColumn(en = "Bill To Address", order = 12)
    private String billToAddress;

    @ExcelColumn(en = "Bill To Email", order = 13)
    private String billToEmail;

    @ExcelColumn(en = "Total Amount", order = 14)
    private BigDecimal totalAmount;

    @ExcelColumn(en = "Exclude Tax Amount", order = 15)
    private BigDecimal excludeTaxAmount;

    @ExcelColumn(en = "Tax Amount", order = 16)
    private BigDecimal taxAmount;

    @ExcelColumn(en = "Delete Flag", order = 17)
    private Integer delFlag;

    @ExcelColumn(en = "Remark", order = 18)
    private String remark;

    @ExcelColumn(en = "Object Version Number", order = 19)
    private Long objectVersionNumber;

    @ExcelColumn(en = "Submit Time", order = 20)
    private Date submitTime;

    @ExcelColumn(en = "Creation Date", order = 21)
    private Date creationDate;

    @ExcelColumn(en = "Created By", order = 22)
    private Long createdBy;

    @ExcelColumn(en = "Last Updated By", order = 23)
    private Long lastUpdatedBy;

    @ExcelColumn(en = "Last Update Date", order = 24)
    private Date lastUpdateDate;

}
