package com.pp1.salve.model.pedido;

import java.util.List;

import com.pp1.salve.model.baseModel.AuditEntityPedido;
import com.pp1.salve.model.endereco.Endereco;
import com.pp1.salve.model.entregador.Entregador;
import com.pp1.salve.model.loja.Loja;
import com.pp1.salve.model.pedido.itemDoPedido.ItemPedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Pedido")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pedido extends AuditEntityPedido{


    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "endereco_entrega_id", nullable = false)
    private Endereco enderecoEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "valor_total", nullable = false)
    private Double valorTotal;

    @Column(name = "taxa_entrega", nullable = false)
    private Double taxaEntrega;

    @Column(name = "forma_pagamento", nullable = false)
    private String formaPagamento;

    @Column(name = "senha",length = 5,nullable = true)
    private String senha;
   

    @OneToMany
    private List<ItemPedido> itens;

    @ManyToOne
    private Loja loja;

    @ManyToOne
    @JoinColumn(name = "entregador_id", nullable = true)
    private Entregador entregador;

    public enum Status {
        PENDENTE, PREPARANDO, AGUARDANDO_ENTREGADOR, A_CAMINHO, ENTREGUE, CANCELADO;
    }
}
