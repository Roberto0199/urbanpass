package com.urbanpass.urbanpass.controller;

import com.urbanpass.urbanpass.entity.Station;
import com.urbanpass.urbanpass.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationRepository stationRepository;

    // GET /api/stations → listar todas las estaciones activas
    @GetMapping
    public ResponseEntity<List<Station>> getAllStations() {
        return ResponseEntity.ok(stationRepository.findByIsActiveTrue());
    }

    // GET /api/stations/{id} → ver una estación específica
    @GetMapping("/{id}")
    public ResponseEntity<Station> getStation(@PathVariable Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estación no encontrada: " + id));
        return ResponseEntity.ok(station);
    }
}