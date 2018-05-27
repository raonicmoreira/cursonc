package com.raoni.cursomc.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.raoni.cursomc.domain.ItemPedido;
import com.raoni.cursomc.domain.PagamentoComBoleto;
import com.raoni.cursomc.domain.Pedido;
import com.raoni.cursomc.domain.enuns.EstadoPagamento;
import com.raoni.cursomc.repositories.ItemPedidoRepository;
import com.raoni.cursomc.repositories.PagamentoRepository;
import com.raoni.cursomc.repositories.PedidoRepository;
import com.raoni.cursomc.repositories.ProdutoRepository;
import com.raoni.cursomc.services.Exception.ObjectNotFoundException;

@Service
public class PedidoService {
	
	@Autowired
	private PedidoRepository repo;
	
	@Autowired
	private BoletoService boletoService;
	
	@Autowired
	private PagamentoRepository pagamentoRepository;
	
	@Autowired
	private ProdutoRepository produtoRepository;
	
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	
	public Pedido find(Integer id) {
		
		Pedido obj = repo.findOne(id);
		if (obj == null) {
			throw new ObjectNotFoundException("Objeto não encontrado! Id: " + id
					+ ", Tipo: " + Pedido.class.getName());
		}
		return obj;
		
		
	}

	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstance(new Date());
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if (obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstance()); 
		}
		obj = repo.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		for (ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setPreco(produtoRepository.findOne(ip.getProduto().getId()).getPreco());
			ip.setPedido(obj);
		}
		itemPedidoRepository.save(obj.getItens());
		return obj;
		
	}
}
