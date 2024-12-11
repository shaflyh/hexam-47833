package com.hand.demo.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hand.demo.infra.constant.LovConst;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;
import org.hzero.export.annotation.ExcelColumn;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * (InvoiceApplyHeader) Entity class
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:46:30
 */
@Getter
@Setter
@ApiModel("")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "todo_invoice_apply_header")
public class InvoiceApplyHeader extends AuditDomain implements Serializable {

    private static final long serialVersionUID = 406628452817312283L;

    // Field Constants
    public static final String FIELD_APPLY_HEADER_ID = "applyHeaderId";
    public static final String FIELD_APPLY_HEADER_NUMBER = "applyHeaderNumber";
    public static final String FIELD_APPLY_STATUS = "applyStatus";
    public static final String FIELD_ATTRIBUTE1 = "attribute1";
    public static final String FIELD_ATTRIBUTE10 = "attribute10";
    public static final String FIELD_ATTRIBUTE11 = "attribute11";
    public static final String FIELD_ATTRIBUTE12 = "attribute12";
    public static final String FIELD_ATTRIBUTE13 = "attribute13";
    public static final String FIELD_ATTRIBUTE14 = "attribute14";
    public static final String FIELD_ATTRIBUTE15 = "attribute15";
    public static final String FIELD_ATTRIBUTE2 = "attribute2";
    public static final String FIELD_ATTRIBUTE3 = "attribute3";
    public static final String FIELD_ATTRIBUTE4 = "attribute4";
    public static final String FIELD_ATTRIBUTE5 = "attribute5";
    public static final String FIELD_ATTRIBUTE6 = "attribute6";
    public static final String FIELD_ATTRIBUTE7 = "attribute7";
    public static final String FIELD_ATTRIBUTE8 = "attribute8";
    public static final String FIELD_ATTRIBUTE9 = "attribute9";
    public static final String FIELD_BILL_TO_ADDRESS = "billToAddress";
    public static final String FIELD_BILL_TO_EMAIL = "billToEmail";
    public static final String FIELD_BILL_TO_PERSON = "billToPerson";
    public static final String FIELD_BILL_TO_PHONE = "billToPhone";
    public static final String FIELD_DEL_FLAG = "delFlag";
    public static final String FIELD_EXCLUDE_TAX_AMOUNT = "excludeTaxAmount";
    public static final String FIELD_INVOICE_COLOR = "invoiceColor";
    public static final String FIELD_INVOICE_TYPE = "invoiceType";
    public static final String FIELD_REMARK = "remark";
    public static final String FIELD_SUBMIT_TIME = "submitTime";
    public static final String FIELD_TAX_AMOUNT = "taxAmount";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_TOTAL_AMOUNT = "totalAmount";

    // Fields
    @ApiModelProperty("PK")
    @Id
    @GeneratedValue
    @ExcelColumn(en = "Apply Header ID", order = 1)
    private Long applyHeaderId;

    @ExcelColumn(en = "Apply Header Number", order = 2)
    private String applyHeaderNumber;

    @ApiModelProperty(value = "(need Value Set) D : Draft S : Success F : Fail C : Canceled")
    @ExcelColumn(en = "Apply Status", order = 3)
    @LovValue(lovCode = LovConst.InvoiceHeader.INV_STATUS)
    private String applyStatus;

    @ApiModelProperty(value = "(need Value Set) R : Red invoice B : Blue invoice")
    @ExcelColumn(en = "Invoice Color", order = 4)
    @LovValue(lovCode = LovConst.InvoiceHeader.INV_COLOR)
    private String invoiceColor;

    @ApiModelProperty(value = "(need Value Set) P : Paper invoice E : E-invoice")
    @ExcelColumn(en = "Invoice Type", order = 5)
    @LovValue(lovCode = LovConst.InvoiceHeader.INV_TYPE)
    private String invoiceType;

    @ApiModelProperty(value = "1 : deleted 0 : normal")
    @ExcelColumn(en = "Delete Flag", order = 6)
    private Integer delFlag;

    @ExcelColumn(en = "Bill to Address", order = 7)
    private String billToAddress;

    @ExcelColumn(en = "Bill to Email", order = 8)
    private String billToEmail;

    @ExcelColumn(en = "Bill to Person", order = 9)
    private String billToPerson;

    @ExcelColumn(en = "Delete Flag", order = 10)
    private String billToPhone;

    @ApiModelProperty(value = "sum(line exclude_tax_amount)")
    @ExcelColumn(en = "Exclude Tax Amount", order = 11)
    private BigDecimal excludeTaxAmount;

    @ApiModelProperty(value = "sum(line tax_amount)")
    @ExcelColumn(en = "Tax Amount", order = 12)
    private BigDecimal taxAmount;

    @ApiModelProperty(value = "sum(line total_amount)")
    @ExcelColumn(en = "Total Amount", order = 13)
    private BigDecimal totalAmount;

    @ExcelColumn(en = "Tenant ID", order = 14)
    private Long tenantId;

    @ExcelColumn(en = "Remark", order = 15)
    private String remark;

    @ExcelColumn(en = "Submit Time", order = 16)
    private Date submitTime;

    private String attribute1;
    private String attribute2;
    private String attribute3;
    private String attribute4;
    private String attribute5;
    private String attribute6;
    private String attribute7;
    private String attribute8;
    private String attribute9;
    private String attribute10;
    private String attribute11;
    private String attribute12;
    private String attribute13;
    private String attribute14;
    private String attribute15;
}
