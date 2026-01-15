package br.gov.mt.seplag.repository;

import br.gov.mt.seplag.entity.AlbumCover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumCoverRepository extends JpaRepository<AlbumCover, Long> {

    List<AlbumCover> findByAlbumId(Long albumId);

    long countByAlbumId(Long albumId);

    void deleteByAlbumId(Long albumId);
}