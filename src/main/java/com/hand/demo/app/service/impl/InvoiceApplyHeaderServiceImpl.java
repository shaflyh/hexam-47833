package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.infra.constant.LovConst;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderDTOMapper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.domain.PageInfo;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author muhammad.shafly@hand-global.com
 * @since 2024-12-03 10:46:31
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private LovAdapter lovAdapter;

    @Override
    public Page<InvoiceApplyHeader> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));
    }

    @Override
    public Page<InvoiceApplyHeaderDTO> selectListWithMeaning(PageRequest pageRequest,
                                                             InvoiceApplyHeader invoiceApplyHeader,
                                                             Long organizationId) {
        // Fetch paginated data
        Page<InvoiceApplyHeader> invoiceApplyHeaderPage = PageHelper.doPageAndSort(pageRequest,
                () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));
        // Transform entities to DTOs
        List<InvoiceApplyHeaderDTO> headerDTOList = new ArrayList<>();
        for (InvoiceApplyHeader invoice : invoiceApplyHeaderPage) {
            InvoiceApplyHeaderDTO dto = InvoiceApplyHeaderDTOMapper.toDTO(invoice);
            headerDTOList.add(dto);
        }
        // Create PageInfo object from PageRequest
        PageInfo pageInfo = new PageInfo(pageRequest.getPage(), pageRequest.getSize());
        return new Page<>(headerDTOList, pageInfo, invoiceApplyHeaderPage.getTotalElements());
    }

    @Override
    public void saveData(List<InvoiceApplyHeader> invoiceApplyHeaders, Long organizationId) {
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
                throw new CommonException("Invalid apply status!");
            }
            if (!validColors.contains(invoice.getInvoiceColor())) {
                throw new CommonException("Invalid invoice color!");
            }
            if (!validTypes.contains(invoice.getInvoiceType())) {
                throw new CommonException("Invalid invoice type!");
            }
        }

        List<InvoiceApplyHeader> insertList =
                invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() == null)
                        .collect(Collectors.toList());
        List<InvoiceApplyHeader> updateList =
                invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null)
                        .collect(Collectors.toList());

        // Validate and update existing records
        if (!updateList.isEmpty()) {
            List<Long> updateIds = updateList.stream()
                    .map(InvoiceApplyHeader::getApplyHeaderId)
                    .collect(Collectors.toList());

            List<Long> existingIds = invoiceApplyHeaderRepository.findExistingIds(updateIds);
            for (Long id : updateIds) {
                if (!existingIds.contains(id)) {
                    throw new CommonException("Record with ID " + id + " does not exist for update!");
                }
            }
            invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);
        }

        // Insert new records
        if (!insertList.isEmpty()) {
            System.out.println("Insert record using header number");
            invoiceApplyHeaderRepository.batchInsertSelective(insertList);
        }
    }
}

