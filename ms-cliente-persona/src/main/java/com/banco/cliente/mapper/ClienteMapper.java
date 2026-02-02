package com.banco.cliente.mapper;

import com.banco.cliente.dto.ClienteRequestDto;
import com.banco.cliente.dto.ClienteResponseDto;
import com.banco.cliente.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteResponseDto toDto(Cliente entity);

    List<ClienteResponseDto> toDtoList(List<Cliente> entities);

    @Mapping(target = "clienteId", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "estado", constant = "true")
    Cliente toEntity(ClienteRequestDto dto);

    @Mapping(target = "clienteId", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    void updateEntity(ClienteRequestDto dto, @MappingTarget Cliente entity);
}
