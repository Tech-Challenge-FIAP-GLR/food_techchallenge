package com.fiap.food_techchallenge.data.adapter;

import com.fiap.food_techchallenge.data.dto.PedidoDTO;
import com.fiap.food_techchallenge.data.entities.ItensPedidoEntity;
import com.fiap.food_techchallenge.data.entities.PedidoEntity;
import com.fiap.food_techchallenge.data.entities.ProdutoEntity;
import com.fiap.food_techchallenge.data.entities.UserEntity;
import com.fiap.food_techchallenge.data.repositories.ItensPedidoJpaRepository;
import com.fiap.food_techchallenge.data.repositories.PedidoJpaRepository;
import com.fiap.food_techchallenge.data.repositories.ProdutoJpaRepository;
import com.fiap.food_techchallenge.domain.constants.ConstantUtils;
import com.fiap.food_techchallenge.domain.enums.OrderStatus;
import com.fiap.food_techchallenge.domain.models.PedidoModel;
import com.fiap.food_techchallenge.domain.models.ProdutoModel;
import com.fiap.food_techchallenge.domain.models.UserModel;
import com.fiap.food_techchallenge.domain.repositories.PedidoRepository;
import jakarta.persistence.NoResultException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class PedidoAdapter implements PedidoRepository {

    private final PedidoJpaRepository pedidoRepository;
    private final ProdutoJpaRepository produtoJpaRepository;
    private final ItensPedidoJpaRepository itensPedidoJpaRepository;

    public PedidoAdapter(PedidoJpaRepository pedidoRepository,
                         ProdutoJpaRepository produtoJpaRepository, ItensPedidoJpaRepository itensPedidoJpaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoJpaRepository = produtoJpaRepository;
        this.itensPedidoJpaRepository = itensPedidoJpaRepository;
    }

    @Override
    @Transactional
    public PedidoModel salvar(PedidoModel pedidoModel, List<Long> produtos) {
        PedidoEntity pedidoEntity = new PedidoEntity();
        pedidoEntity.setUserEntity(new UserEntity(pedidoModel.getUserModel().getId(), pedidoModel.getUserModel().getNome(), pedidoModel.getUserModel().getCpf(), pedidoModel.getUserModel().getEmail()));
        pedidoEntity.setDatapedido(LocalDateTime.now());
        pedidoEntity.setTotal(pedidoModel.getTotal());
        pedidoEntity.setOrderStatus(OrderStatus.RECEIVED.name());
        pedidoEntity.setUuid(pedidoModel.getUuid());

        boolean requestMercadoPago = true;
//
//        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
////        String url = "https://api.mercadopago.com/instore/orders/qr/seller/collectors/" + ConstantUtils.USER_ID + "/pos/" + ConstantUtils.EXTERNAL_POS_ID + "/qrs";
//        String url = "https://chart.googleapis.com/chart?cht=qr&chs=200x200&chl=12331";
//        try {
//            HttpGet request = new HttpGet(url);
//
//        }catch (Exception e){
//            System.out.print(e.toString());
//        }


        if (requestMercadoPago){
            pedidoEntity.setPaymentStatus("APROVADO");
        }

        var pedidoRetorno = pedidoRepository.save(pedidoEntity);
        pedidoModel.setId(pedidoRetorno.getId());
        pedidoModel.setDatapedido(pedidoRetorno.getDatapedido());
        pedidoModel.setOrderStatus(pedidoRetorno.getOrderStatus());
        pedidoModel.setPaymentStatus(pedidoRetorno.getPaymentStatus());
        for (Long produto : produtos) {
            Optional<ProdutoEntity> produtoRetorno = produtoJpaRepository.findById(produto);
            if(produtoRetorno.isPresent()){
                itensPedidoJpaRepository.save(new ItensPedidoEntity(produtoRetorno.get(), pedidoRetorno));
            }
            else{
                throw new NoResultException();
            }
        }

        return pedidoModel;
    }

    @Override
    public List<PedidoDTO> listaPedidoStatus(String status) {
        try{
            List<PedidoDTO> pedidosResult = new ArrayList<>();
            var pedidos = pedidoRepository.findAllByOrderStatus(status);
            for (PedidoEntity pedido : pedidos) {
                List<ProdutoModel> produtosResult = new ArrayList<>();
                List<ItensPedidoEntity> itensProduto = itensPedidoJpaRepository.findAllByPedidoEntity(pedido);
                for (ItensPedidoEntity item : itensProduto){
                    produtosResult.add(ProdutoModel.fromEntity(item.getProdutoEntity()));
                }
                pedidosResult.add(new PedidoDTO(pedido.getId(), pedido.getUuid(), UserModel.fromEntity(pedido.getUserEntity()), pedido.getDatapedido(),
                        pedido.getTotal(), pedido.getOrderStatus(), produtosResult, pedido.getPaymentStatus()));
            }
            return pedidosResult;
        } catch (Exception e) {
            throw new NoResultException();
        }
    }

    @Override
    public List<PedidoDTO> listaPedidos() {
        try{
            List<PedidoDTO> pedidosResult = new ArrayList<>();
            List<PedidoDTO> pedidosReceived = new ArrayList<>();
            List<PedidoDTO> pedidosPreparation = new ArrayList<>();
            List<PedidoDTO> pedidosReady = new ArrayList<>();
            var pedidos = pedidoRepository.findAllPedidosAtivos();
            for (PedidoEntity pedido : pedidos) {
                List<ProdutoModel> produtosResult = new ArrayList<>();
                List<ItensPedidoEntity> itensProduto = itensPedidoJpaRepository.findAllByPedidoEntity(pedido);
                for (ItensPedidoEntity item : itensProduto){
                    produtosResult.add(ProdutoModel.fromEntity(item.getProdutoEntity()));
                }
                if(pedido.getOrderStatus().equals("RECEIVED")){
                    pedidosReceived.add(new PedidoDTO(pedido.getId(), pedido.getUuid(), UserModel.fromEntity(pedido.getUserEntity()), pedido.getDatapedido(),
                            pedido.getTotal(), pedido.getOrderStatus(), produtosResult, pedido.getPaymentStatus()));
                }
                if(pedido.getOrderStatus().equals("IN_PREPARATION")){
                    pedidosPreparation.add(new PedidoDTO(pedido.getId(), pedido.getUuid(), UserModel.fromEntity(pedido.getUserEntity()), pedido.getDatapedido(),
                            pedido.getTotal(), pedido.getOrderStatus(), produtosResult, pedido.getPaymentStatus()));
                }
                if(pedido.getOrderStatus().equals("READY")){
                    pedidosReady.add(new PedidoDTO(pedido.getId(), pedido.getUuid(), UserModel.fromEntity(pedido.getUserEntity()), pedido.getDatapedido(),
                            pedido.getTotal(), pedido.getOrderStatus(), produtosResult, pedido.getPaymentStatus()));
                }

                pedidosResult = Stream.concat(pedidosReady.stream(),pedidosPreparation.stream()).collect(Collectors.toList());
                pedidosResult = Stream.concat(pedidosResult.stream(),pedidosReceived.stream()).collect(Collectors.toList());
            }
            return pedidosResult;
        } catch (Exception e) {
            throw new NoResultException();
        }
    }

    @Override
    @Transactional
    public PedidoModel atualizaStatus(Long pedidoId) {
        PedidoEntity pedidoEntity = pedidoRepository.findById(pedidoId).get();
        if(pedidoEntity.getOrderStatus().equals("RECEIVED")){
            pedidoEntity.setOrderStatus(OrderStatus.IN_PREPARATION.name());
            return PedidoModel.fromEntity(pedidoRepository.save(pedidoEntity));
        }
        if(pedidoEntity.getOrderStatus().equals("IN_PREPARATION")){
            pedidoEntity.setOrderStatus(OrderStatus.READY.name());
            return PedidoModel.fromEntity(pedidoRepository.save(pedidoEntity));
        }
        if(pedidoEntity.getOrderStatus().equals("READY")){
            pedidoEntity.setOrderStatus(OrderStatus.COMPLETED.name());
            return PedidoModel.fromEntity(pedidoRepository.save(pedidoEntity));
        }

        return PedidoModel.fromEntity(pedidoRepository.save(pedidoEntity));
    }

    @Override
    public PedidoDTO buscaStatusPagamento(Long pedidoId) {
        var p = pedidoRepository.findById(pedidoId);
        PedidoDTO p2 = new PedidoDTO();
        p2.setPaymentStatus(p.get().getPaymentStatus());
        p2.setUserModel(UserModel.fromEntity(p.get().getUserEntity()));
        p2.setTotal(p.get().getTotal());
        p2.setId(p.get().getId());
        p2.setDatapedido(p.get().getDatapedido());
        p2.setUuid(p.get().getUuid());

        List<ItensPedidoEntity> itensProduto = itensPedidoJpaRepository.findAllByPedidoEntity(p.get());
        List<ProdutoModel> produtosResult = new ArrayList<>();
        for (ItensPedidoEntity item : itensProduto){
            produtosResult.add(ProdutoModel.fromEntity(item.getProdutoEntity()));
        }
        p2.setProdutoModels(produtosResult);
        p2.setOrderStatus(p.get().getOrderStatus());
        return p2;
    }


}
