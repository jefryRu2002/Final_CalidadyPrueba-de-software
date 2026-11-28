package com.reparaciones.paginaweb.controllers;

import com.reparaciones.paginaweb.models.OrdenTrabajo;
import com.reparaciones.paginaweb.services.OrdenTrabajoService;
import com.reparaciones.paginaweb.services.NotificacionService;
import com.reparaciones.paginaweb.repositories.CotizacionRepository;
import com.reparaciones.paginaweb.repositories.ReparacionRepuestoRepository;
import com.reparaciones.paginaweb.repositories.ReparacionTrabajoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenTrabajoControllerTest {

    @Mock
    private OrdenTrabajoService ordenTrabajoService;
    @Mock
    private CotizacionRepository cotizacionRepository;
    @Mock
    private NotificacionService notificacionService;
    @Mock
    private ReparacionTrabajoRepository reparacionTrabajoRepository;
    @Mock
    private ReparacionRepuestoRepository reparacionRepuestoRepository;

    @InjectMocks
    private OrdenTrabajoController controller;

    @Test
    void updateRejectsTechnicianChangesAfterOrderIsAssigned() {
        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setEstado("EN_REPARACION");
        when(ordenTrabajoService.findById(5)).thenReturn(orden);

        ResponseEntity<?> response = controller.update(5, Map.of("idTecnico", 8));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(ordenTrabajoService, never()).update(anyInt(), any(OrdenTrabajo.class));
    }

    @Test
    void updateAllowsTechnicianChangeWithoutChangingAssignedState() {
        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setEstado("ASIGNADO");
        when(ordenTrabajoService.findById(5)).thenReturn(orden);
        when(ordenTrabajoService.update(eq(5), any(OrdenTrabajo.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        ResponseEntity<?> response = controller.update(5, Map.of("idTecnico", 8));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ArgumentCaptor<OrdenTrabajo> ordenCaptor = ArgumentCaptor.forClass(OrdenTrabajo.class);
        verify(ordenTrabajoService).update(eq(5), ordenCaptor.capture());
        assertEquals("ASIGNADO", ordenCaptor.getValue().getEstado());
        assertEquals(8, ordenCaptor.getValue().getTecnico().getIdPersona());
    }
}
