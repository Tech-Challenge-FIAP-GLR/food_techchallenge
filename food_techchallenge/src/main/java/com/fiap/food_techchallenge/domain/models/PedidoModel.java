package com.fiap.food_techchallenge.domain.models;


import com.fiap.food_techchallenge.data.entities.PedidoEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
public class PedidoModel {

    private Long id;
    private String uuid;
    private UserModel userModel;
    private LocalDateTime datapedido;
    private Float total;
    private String orderStatus;
    private String paymentStatus;

    public PedidoModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public static PedidoModel fromEntity(PedidoEntity pedidoEntity) {
        return new PedidoModel(pedidoEntity.getId(),
                pedidoEntity.getUuid(),
                UserModel.fromEntity(pedidoEntity.getUserEntity()),
                pedidoEntity.getDatapedido(),
                pedidoEntity.getTotal(),
                pedidoEntity.getOrderStatus(),
                pedidoEntity.getPaymentStatus());
    }

    public PedidoModel(Long id, String uuid, UserModel userModel, LocalDateTime datapedido, Float total) {
        this.id = id;
        this.uuid = uuid;
        this.userModel = userModel;
        this.datapedido = datapedido;
        this.total = total == null ? (float) 0.0 : total;
    }

    public PedidoModel(UserModel userModel, LocalDateTime datapedido, Float total) {
        this.userModel = userModel;
        this.datapedido = datapedido;
        this.total = total == null ? (float) 0.0 : total;
    }

    public PedidoModel(Long id, String uuid, UserModel userModel, LocalDateTime datapedido, Float total, String orderStatus, String paymentStatus) {
        this.id = id;
        this.uuid = uuid;
        this.userModel = userModel;
        this.datapedido = datapedido;
        this.total = total == null ? (float) 0.0 : total;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
    }

    public PedidoModel(UserModel userModel, LocalDateTime datapedido, Float total, String orderStatus) {
        this.userModel = userModel;
        this.datapedido = datapedido;
        this.total = total == null ? (float) 0.0 : total;
        this.orderStatus = orderStatus;
    }
}


