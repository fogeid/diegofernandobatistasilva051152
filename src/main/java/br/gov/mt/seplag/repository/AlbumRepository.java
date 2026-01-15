package br.gov.mt.seplag.repository;

import br.gov.mt.seplag.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("SELECT DISTINCT a FROM Album a JOIN a.artists ar WHERE ar.isBand = true")
    Page<Album> findAlbumsByBands(Pageable pageable);

    @Query("SELECT DISTINCT a FROM Album a JOIN a.artists ar WHERE ar.isBand = false")
    Page<Album> findAlbumsBySoloArtists(Pageable pageable);


    @Query("SELECT DISTINCT a FROM Album a LEFT JOIN FETCH a.artists")
    List<Album> findAllWithArtists();


    List<Album> findByReleaseYear(Integer releaseYear);


    List<Album> findByTitleContainingIgnoreCase(String title);
}