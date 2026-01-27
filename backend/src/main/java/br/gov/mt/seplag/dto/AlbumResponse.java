package br.gov.mt.seplag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponse {

    private Long id;
    private String title;
    private Integer releaseYear;


    private List<ArtistResponse> artists;

    private List<AlbumCoverResponse> covers;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}