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
public class AlbumSimpleResponse {

    private Long id;
    private String title;
    private Integer releaseYear;
    private LocalDateTime createdAt;
}