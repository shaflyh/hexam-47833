package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Shafly - 47833
 * @since 2024-12-16 10:53
 */
@Getter
@Setter
public class HfleUploadConfigDTO {
    private String bucketName;
    private String directory;
    private String contentType;
    private String storageUnit;
    private Long storageSize;
}
