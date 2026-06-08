package com.sistemabarberia.fadex_backend.modules.venta.mapper;

import com.sistemabarberia.fadex_backend.modules.barbero.entity.Barbero;
import com.sistemabarberia.fadex_backend.modules.cliente.entity.Cliente;
import com.sistemabarberia.fadex_backend.modules.venta.dto.request.VentaRequestDTO;
import com.sistemabarberia.fadex_backend.modules.venta.dto.response.VentaResponseDTO;
import com.sistemabarberia.fadex_backend.modules.venta.entity.Venta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VentaMapper {

    @Mapping(target = "ventaId", ignore = true)
    @Mapping(target = "cliente", expression = "java(mapCliente(dto.getClienteId()))")
    @Mapping(target = "barbero", expression = "java(mapBarbero(dto.getBarberoId()))")
    Venta toEntity(VentaRequestDTO dto);

    @Mapping(source = "venta.cliente.persona.nombre", target = "clienteNombre")
    @Mapping(source = "venta.barbero.persona.nombre", target = "barberoNombre")
    @Mapping(source = "venta.cliente.clienteId", target = "clienteId")
    @Mapping(source = "venta.barbero.barberoId", target = "barberoId")
    VentaResponseDTO toResponse(Venta venta);

    List<VentaResponseDTO> toResponseList(List<Venta> ventas);

    default Cliente mapCliente(Integer id) {
        if (id == null) return null;
        Cliente cliente = new Cliente();
        cliente.setClienteId(id);
        return cliente;
    }

    default Barbero mapBarbero(Integer id) {
        if (id == null) return null;
        Barbero barbero = new Barbero();
        barbero.setBarberoId(id);
        return barbero;
    }
}