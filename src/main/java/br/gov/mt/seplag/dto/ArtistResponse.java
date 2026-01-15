package br.gov.mt.seplag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistResponse {

    private Long id;
    private String name;
    private Boolean isBand;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}