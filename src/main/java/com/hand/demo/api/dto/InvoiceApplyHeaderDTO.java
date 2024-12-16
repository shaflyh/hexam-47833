package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.Getter;
import lombok.Setter;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

import javax.persistence.Transient;
import java.util.List;

/**
 * @author Shafly - 47833
 * @since 2024-12-03 14:30
 */
@Getter
@Setter
@ExcelSheet(en = "Invoice Apply Header")
public class InvoiceApplyHeaderDTO extends InvoiceApplyHeader {

    @ExcelColumn(en = "Apply Status Meaning", order = 17)
    @Transient
    private String applyStatusMeaning;

    @ExcelColumn(en = "Invoice Color Meaning", order = 18)
    @Transient
    private String invoiceColorMeaning;

    @ExcelColumn(en = "Invoice Type Meaning", order = 19)
    @Transient
    private String invoiceTypeMeaning;

    @ExcelColumn(promptCode = "children", promptKey = "children", child = true, order = 20)
    @Transient
    private List<InvoiceApplyLine> invoiceApplyLineList;

    @Transient
    private String requester;

    @Transient
    private String invoiceApplyLineNames;

    private boolean tenantAdminFlag;
}
