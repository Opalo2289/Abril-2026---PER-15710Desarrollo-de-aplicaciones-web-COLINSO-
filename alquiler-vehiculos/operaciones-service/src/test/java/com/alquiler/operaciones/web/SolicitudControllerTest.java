package com.alquiler.operaciones.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SolicitudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registrarConfirmarCancelarYFiltros() throws Exception {
        mockMvc.perform(post("/api/operaciones/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehiculoId": 10,
                                  "fechaInicio": "2030-06-01",
                                  "fechaFin": "2030-06-05"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        mockMvc.perform(get("/api/operaciones/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/operaciones/solicitudes").param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(post("/api/operaciones/solicitudes/1/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        mockMvc.perform(post("/api/operaciones/solicitudes/1/cancelar"))
                .andExpect(status().isConflict());
    }

    @Test
    void solapeMismaVentana409() throws Exception {
        String body = """
                {
                  "vehiculoId": 20,
                  "fechaInicio": "2031-01-01",
                  "fechaFin": "2031-01-10"
                }
                """;
        mockMvc.perform(post("/api/operaciones/solicitudes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/operaciones/solicitudes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminarPorVehiculoBorraSolicitudes() throws Exception {
        String body = """
                {
                  "vehiculoId": 99,
                  "fechaInicio": "2030-07-01",
                  "fechaFin": "2030-07-05"
                }
                """;
        mockMvc.perform(post("/api/operaciones/solicitudes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/operaciones/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/api/operaciones/solicitudes/vehiculo/99"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/operaciones/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void fechasInvertidas400() throws Exception {
        mockMvc.perform(post("/api/operaciones/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehiculoId": 1,
                                  "fechaInicio": "2032-12-31",
                                  "fechaFin": "2032-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
