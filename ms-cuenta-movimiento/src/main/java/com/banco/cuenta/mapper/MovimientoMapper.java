package com.banco.cuenta.mapper;

import com.banco.cuenta.dto.MovimientoRequestDto;
import com.banco.cuenta.dto.MovimientoResponseDto;
import com.banco.cuenta.entity.Movimiento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MovimientoMapper {

    MovimientoResponseDto toDto(Movimiento entity);

    List<MovimientoResponseDto> toDtoList(List<Movimiento> entities);

    @Mapping(target = "movimientoId", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "saldoAnterior", ignore = true)
    @Mapping(target = "saldoDespues", ignore = true)
    Movimiento toEntity(MovimientoRequestDto dto);
}
