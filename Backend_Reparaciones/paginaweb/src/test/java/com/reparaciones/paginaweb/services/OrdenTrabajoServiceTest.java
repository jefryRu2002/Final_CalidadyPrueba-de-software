package com.reparaciones.paginaweb.services;

import com.reparaciones.paginaweb.models.EquipoModel;
import com.reparaciones.paginaweb.models.OrdenTrabajo;
import com.reparaciones.paginaweb.models.ReparacionRepuestoModel;
import com.reparaciones.paginaweb.models.ReparacionTrabajoModel;
import com.reparaciones.paginaweb.models.ReporteFallaModel;
import com.reparaciones.paginaweb.models.TecnicoModel;
import com.reparaciones.paginaweb.repositories.EquipoRepository;
import com.reparaciones.paginaweb.repositories.OrdenTrabajoRepository;
import com.reparaciones.paginaweb.repositories.ReparacionRepuestoRepository;
import com.reparaciones.paginaweb.repositories.ReparacionTrabajoRepository;
import com.reparaciones.paginaweb.repositories.ReporteFallaRepository;
import com.reparaciones.paginaweb.repositories.TecnicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenTrabajoServiceTest {

    @Mock
    private OrdenTrabajoRepository ordenTrabajoRepository;
    @Mock
    private ReporteFallaRepository reporteFallaRepository;
    @Mock
    private EquipoRepository equipoRepository;
    @Mock
    private TecnicoRepository tecnicoRepository;
    @Mock
    private ReparacionTrabajoRepository reparacionTrabajoRepository;
    @Mock
    private ReparacionRepuestoRepository reparacionRepuestoRepository;

    @InjectMocks
    private OrdenTrabajoService ordenTrabajoService;

    @Test
    void saveAppliesDefaultDateAndStatus() {
        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setEstado(null);
        when(ordenTrabajoRepository.save(orden)).thenReturn(orden);

        OrdenTrabajo saved = ordenTrabajoService.save(orden);

        assertEquals("PENDIENTE", saved.getEstado());
        assertNotNull(saved.getFechaInicio());
    }

    @Test
    void updateChangesProvidedFieldsAndKeepsExistingValuesForNullFields() {
        OrdenTrabajo existing = new OrdenTrabajo();
        existing.setEstado("ASIGNADO");
        existing.setFechaInicio(LocalDate.of(2026, 1, 10));
        existing.setNotasTecnico("Nota existente");

        OrdenTrabajo changes = new OrdenTrabajo();
        changes.setEstado("EN_REPARACION");
        changes.setNotasTecnico(null);

        when(ordenTrabajoRepository.findById(5)).thenReturn(Optional.of(existing));
        when(ordenTrabajoRepository.save(existing)).thenReturn(existing);

        OrdenTrabajo updated = ordenTrabajoService.update(5, changes);

        assertEquals("EN_REPARACION", updated.getEstado());
        assertEquals(LocalDate.of(2026, 1, 10), updated.getFechaInicio());
        assertEquals("Nota existente", updated.getNotasTecnico());
    }

    @Test
    void terminarReparacionEnablesPaymentAndSetsCompletionDate() {
        OrdenTrabajo existing = new OrdenTrabajo();
        when(ordenTrabajoRepository.findById(5)).thenReturn(Optional.of(existing));
        when(ordenTrabajoRepository.save(existing)).thenReturn(existing);

        OrdenTrabajo completed = ordenTrabajoService.terminarReparacion(5);

        assertEquals("TERMINADO", completed.getEstado());
        assertTrue(completed.getPuedePagar());
        assertEquals(LocalDate.now(), completed.getFechaFin());
    }

    @Test
    void asignarTecnicoSetsAssignmentStateAndTimestamp() {
        OrdenTrabajo orden = new OrdenTrabajo();
        TecnicoModel tecnico = new TecnicoModel();
        when(ordenTrabajoRepository.findById(5)).thenReturn(Optional.of(orden));
        when(tecnicoRepository.findById(8)).thenReturn(Optional.of(tecnico));
        when(ordenTrabajoRepository.save(orden)).thenReturn(orden);

        OrdenTrabajo assigned = ordenTrabajoService.asignarTecnico(5, 8);

        assertEquals(tecnico, assigned.getTecnico());
        assertEquals("ASIGNADO", assigned.getEstado());
        assertNotNull(assigned.getFechaAsignacion());
    }

    @Test
    void guardarDetallesReemplazaTrabajosYRepuestosDeLaOrden() {
        OrdenTrabajo orden = new OrdenTrabajo();
        ReparacionTrabajoModel trabajoAnterior = new ReparacionTrabajoModel();
        ReparacionRepuestoModel repuestoAnterior = new ReparacionRepuestoModel();
        orden.getTrabajosRealizados().add(trabajoAnterior);
        orden.getRepuestosUsados().add(repuestoAnterior);

        ReparacionTrabajoModel trabajoNuevo = new ReparacionTrabajoModel();
        trabajoNuevo.setId(12);
        ReparacionRepuestoModel repuestoNuevo = new ReparacionRepuestoModel();
        repuestoNuevo.setId(13);

        when(ordenTrabajoRepository.findById(5)).thenReturn(Optional.of(orden));
        when(ordenTrabajoRepository.save(orden)).thenReturn(orden);

        OrdenTrabajo updated = ordenTrabajoService.guardarDetallesReparacion(
                5, List.of(trabajoNuevo), List.of(repuestoNuevo), "Reparación realizada");

        verify(reparacionTrabajoRepository).deleteByOrdenIdOrden(5);
        verify(reparacionRepuestoRepository).deleteByOrdenIdOrden(5);
        assertEquals(List.of(trabajoNuevo), updated.getTrabajosRealizados());
        assertEquals(List.of(repuestoNuevo), updated.getRepuestosUsados());
        assertEquals(orden, trabajoNuevo.getOrden());
        assertEquals(orden, repuestoNuevo.getOrden());
        assertNull(trabajoNuevo.getId());
        assertNull(repuestoNuevo.getId());
        assertEquals("Reparación realizada", updated.getNotasTecnico());
    }

    @Test
    void guardarDetallesSinRepuestosConservaLosRepuestosExistentes() {
        OrdenTrabajo orden = new OrdenTrabajo();
        ReparacionRepuestoModel repuestoCotizado = new ReparacionRepuestoModel();
        orden.getRepuestosUsados().add(repuestoCotizado);
        when(ordenTrabajoRepository.findById(5)).thenReturn(Optional.of(orden));
        when(ordenTrabajoRepository.save(orden)).thenReturn(orden);

        OrdenTrabajo updated = ordenTrabajoService.guardarDetallesReparacion(
                5, List.of(), null, "Notas actualizadas");

        verify(reparacionRepuestoRepository, never()).deleteByOrdenIdOrden(5);
        assertEquals(List.of(repuestoCotizado), updated.getRepuestosUsados());
        assertEquals("Notas actualizadas", updated.getNotasTecnico());
    }

    @Test
    void crearDesdeReporteBuildsOrderWithCopiedEquipmentData() {
        ReporteFallaModel reporte = new ReporteFallaModel();
        reporte.setMarca("Dell");
        reporte.setModelo("Latitude");
        reporte.setTipoEquipo("Laptop");
        reporte.setNumeroSerie("SN-123");

        when(reporteFallaRepository.findById(3)).thenReturn(Optional.of(reporte));
        when(equipoRepository.save(any(EquipoModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(ordenTrabajoRepository.save(any(OrdenTrabajo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrdenTrabajo created = ordenTrabajoService.crearDesdeReporte(3);

        assertEquals(reporte, created.getReporte());
        assertEquals("Dell", created.getEquipo().getMarca());
        assertEquals("Latitude", created.getEquipo().getModelo());
        assertEquals("Laptop", created.getEquipo().getTipo());
        assertEquals("SN-123", created.getEquipo().getSerie());
        assertEquals("PENDIENTE", created.getEstado());
        assertNotNull(created.getFechaInicio());
    }

    @Test
    void findByIdReportsMissingOrder() {
        when(ordenTrabajoRepository.findById(5)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> ordenTrabajoService.findById(5));

        assertEquals("Orden no encontrada: 5", exception.getMessage());
    }
}
