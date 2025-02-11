package com.pp1.salve.model.pedido;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pp1.salve.model.entregador.Entregador;
import com.pp1.salve.model.loja.Loja;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Page<Pedido> findByCriadoPorId(String id, Pageable pageable);

    Page<Pedido> findByLoja(Loja loja, Pageable pageable);

    boolean existsByEntregadorAndStatusNot(Entregador entregador, Pedido.Status status);

    Page<Pedido> findByLojaAndStatusOrderByDataPedidoDesc(Loja loja, Pedido.Status status, Pageable pageable);
  }