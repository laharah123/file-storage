package com.irina.filestorage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FileSearchRequest {
    private String fileNameRegex;
    private Integer pageSize;
    private Integer pageNumber;
}
