package com.banco.cuenta;

import com.banco.cuenta.dto.CuentaRequestDto;
import com.banco.cuenta.dto.MovimientoRequestDto;
import com.banco.cuenta.enums.TipoCuenta;
import com.banco.cuenta.enums.TipoMovimiento;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovimientoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String numeroCuentaTest;

    @BeforeEach
    void setUp() throws Exception {
        // Crear una cuenta de prueba
        CuentaRequestDto cuentaDto = CuentaRequestDto.builder()
                .tipoCuenta(TipoCuenta.AHORRO)
                .saldoInicial(new BigDecimal("1000.00"))
                .estado(true)
                .clienteId("CLI-TEST001")
                .build();

        String cuentaJson = objectMapper.writeValueAsString(cuentaDto);

        MvcResult result = mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuentaJson))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        numeroCuentaTest = objectMapper.readTree(responseJson)
                .path("data")
                .path("numeroCuenta")
                .asText();
    }

    @Test
    void cuandoRegistrarDepositoValido_debeRetornar201YActualizarSaldo() throws Exception {

        MovimientoRequestDto movimientoDto = MovimientoRequestDto.builder()
                .numeroCuenta(numeroCuentaTest)
                .tipoMovimiento(TipoMovimiento.DEPOSITO)
                .valor(new BigDecimal("500.00"))
                .descripcion("Deposito de prueba")
                .build();

        String movimientoJson = objectMapper.writeValueAsString(movimientoDto);

        mockMvc.perform(post("/api/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimientoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.movimientoId").exists())
                .andExpect(jsonPath("$.data.tipoMovimiento").value("DEPOSITO"))
                .andExpect(jsonPath("$.data.valor").value(500.00))
                .andExpect(jsonPath("$.data.saldoAnterior").value(1000.00))
                .andExpect(jsonPath("$.data.saldoDespues").value(1500.00))
                .andExpect(jsonPath("$.data.numeroCuenta").value(numeroCuentaTest));
    }

    @Test
    void cuandoRegistrarRetiroValido_debeRetornar201YDecrementarSaldo() throws Exception {
        MovimientoRequestDto movimientoDto = MovimientoRequestDto.builder()
                .numeroCuenta(numeroCuentaTest)
                .tipoMovimiento(TipoMovimiento.RETIRO)
                .valor(new BigDecimal("300.00"))
                .descripcion("Retiro de prueba")
                .build();

        String movimientoJson = objectMapper.writeValueAsString(movimientoDto);

        mockMvc.perform(post("/api/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimientoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tipoMovimiento").value("RETIRO"))
                .andExpect(jsonPath("$.data.valor").value(300.00))
                .andExpect(jsonPath("$.data.saldoDespues").value(700.00));
    }

    @Test
    void cuandoRegistrarRetiroConSaldoInsuficiente_debeRetornar400() throws Exception {
        MovimientoRequestDto movimientoDto = MovimientoRequestDto.builder()
                .numeroCuenta(numeroCuentaTest)
                .tipoMovimiento(TipoMovimiento.RETIRO)
                .valor(new BigDecimal("2000.00")) // Mayor al saldo disponible
                .descripcion("Retiro excesivo")
                .build();

        String movimientoJson = objectMapper.writeValueAsString(movimientoDto);

        mockMvc.perform(post("/api/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimientoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Saldo Insuficiente"))
                .andExpect(jsonPath("$.message").value("Saldo no disponible"));
    }

    @Test
    void cuandoRegistrarMovimientoEnCuentaInexistente_debeRetornar404() throws Exception {

        MovimientoRequestDto movimientoDto = MovimientoRequestDto.builder()
                .numeroCuenta("CTA-NOEXISTE")
                .tipoMovimiento(TipoMovimiento.DEPOSITO)
                .valor(new BigDecimal("100.00"))
                .build();

        String movimientoJson = objectMapper.writeValueAsString(movimientoDto);

        mockMvc.perform(post("/api/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimientoJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void cuandoListarMovimientosConPaginacion_debeRetornarPaginaCorrecta() throws Exception {

        for (int i = 1; i <= 3; i++) {
            MovimientoRequestDto movimientoDto = MovimientoRequestDto.builder()
                    .numeroCuenta(numeroCuentaTest)
                    .tipoMovimiento(TipoMovimiento.DEPOSITO)
                    .valor(new BigDecimal("100.00"))
                    .descripcion("Movimiento " + i)
                    .build();

            mockMvc.perform(post("/api/movimientos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(movimientoDto)));
        }

        mockMvc.perform(get("/api/movimientos")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "fecha,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(5));
    }

    @Test
    void cuandoObtenerMovimientoPorId_debeRetornarMovimiento() throws Exception {

        MovimientoRequestDto movimientoDto = MovimientoRequestDto.builder()
                .numeroCuenta(numeroCuentaTest)
                .tipoMovimiento(TipoMovimiento.DEPOSITO)
                .valor(new BigDecimal("250.00"))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movimientoDto)))
                .andReturn();

        String movimientoId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("movimientoId")
                .asText();

        mockMvc.perform(get("/api/movimientos/" + movimientoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.movimientoId").value(movimientoId))
                .andExpect(jsonPath("$.data.valor").value(250.00));
    }
}
