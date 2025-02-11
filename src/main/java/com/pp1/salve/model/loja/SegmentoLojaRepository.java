package com.pp1.salve.model.loja;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface SegmentoLojaRepository extends JpaRepository<SegmentoLoja, Long> {
    @Query("SELECT s FROM SegmentoLoja s WHERE LOWER(s.nome) = LOWER(?1)")
    List<SegmentoLoja> findByNome(String nome);

    @Query("SELECT DISTINCT s FROM SegmentoLoja s WHERE s IN (SELECT l.segmentoLoja FROM Loja l)")
    List<SegmentoLoja> findAllUsedSegments();


}
