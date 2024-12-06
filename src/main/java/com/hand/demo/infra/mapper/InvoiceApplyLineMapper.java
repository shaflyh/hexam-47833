package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.InvoiceApplyLine;

import java.util.List;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author Shafly - 47833
 * @since 2024-12-03 10:47:02
 */
public interface InvoiceApplyLineMapper extends BaseMapper<InvoiceApplyLine> {
    /**
     * 基础查询
     *
     * @param invoiceApplyLine 查询条件
     * @return 返回值
     */
    List<InvoiceApplyLine> selectList(InvoiceApplyLine invoiceApplyLine);

    /**
     * Select invoice line by header id
     *
     * @param applyHeaderId Invoice apply header id
     * @return List InvoiceApplyLine
     */
    List<InvoiceApplyLine> selectLinesByHeaderId(Long applyHeaderId);
}

