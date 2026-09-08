package com.belajar.springboot.at.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecuteAutoTransferResponse {

    private int terproses;
    private int sukses;
    private int gagal;

    @Builder.Default
    private List<String> rincian = new ArrayList<>();
}