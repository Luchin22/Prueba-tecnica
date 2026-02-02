package com.banco.cuenta.mapper;

import com.banco.cuenta.dto.CuentaRequestDto;
import com.banco.cuenta.dto.CuentaResponseDto;
import com.banco.cuenta.entity.Cuenta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CuentaMapper {

    CuentaResponseDto toDto(Cuenta entity);

    List<CuentaResponseDto> toDtoList(List<Cuenta> entities);

    @Mapping(target = "numeroCuenta", ignore = true)
    @Mapping(target = "saldoActual", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "version", ignore = true)
    Cuenta toEntity(CuentaRequestDto dto);

    @Mapping(target = "numeroCuenta", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(CuentaRequestDto dto, @MappingTarget Cuenta entity);
}
