package br.gov.mt.seplag.repository;

import br.gov.mt.seplag.entity.Regional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionalRepository extends JpaRepository<Regional, Integer> {

    List<Regional> findByAtivoTrue();

    List<Regional> findByAtivoFalse();

    List<Regional> findByNome(String nome);

    Regional findByIdAndAtivoTrue(Integer id);
}
