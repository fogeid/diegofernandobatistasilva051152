package br.gov.mt.seplag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumRequest {

    @NotBlank(message = "Título do álbum é obrigatório")
    private String title;

    private Integer releaseYear;

    @NotEmpty(message = "Pelo menos um artista é obrigatório")
    private Set<Long> artistIds;
}