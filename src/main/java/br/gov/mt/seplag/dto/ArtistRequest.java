package br.gov.mt.seplag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistRequest {

    @NotBlank(message = "Nome do artista é obrigatório")
    private String name;

    private Boolean isBand;
}