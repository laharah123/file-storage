package com.irina.filestorage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileSearchResponse {
    private Integer pageSize;
    private Integer pageNumber;
    private List<String> fileNames;
}
