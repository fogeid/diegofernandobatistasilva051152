package br.gov.mt.seplag.repository;

import br.gov.mt.seplag.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    List<Artist> findByNameContainingIgnoreCase(String name);

    @Query("SELECT a FROM Artist a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "ORDER BY CASE WHEN :sort = 'asc' THEN a.name END ASC, " +
            "         CASE WHEN :sort = 'desc' THEN a.name END DESC")
    List<Artist> searchByNameWithSort(@Param("name") String name, @Param("sort") String sort);

    List<Artist> findByIsBandTrue();

    List<Artist> findByIsBandFalse();
}
