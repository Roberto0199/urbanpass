package com.urbanpass.urbanpass.config;

import com.urbanpass.urbanpass.entity.Station;
import com.urbanpass.urbanpass.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final StationRepository stationRepository;

    @Override
    public void run(String... args) {
        // Solo carga si la tabla está vacía
        if (stationRepository.count() == 0) {
            log.info("Cargando estaciones...");

            List<Station> stations = List.of(
                    Station.builder().name("Centra Norte").location("Zona 18").isActive(true).build(),
                    Station.builder().name("Minerva").location("Zona 11").isActive(true).build(),
                    Station.builder().name("Plaza Barrios").location("Zona 1").isActive(true).build(),
                    Station.builder().name("Las Charcas").location("Zona 11").isActive(true).build(),
                    Station.builder().name("Trébol").location("Zona 7").isActive(true).build(),
                    Station.builder().name("El Rosario").location("Zona 5").isActive(true).build(),
                    Station.builder().name("Centro Cívico").location("Zona 1").isActive(true).build(),
                    Station.builder().name("San Pedrito").location("Zona 1").isActive(true).build()
            );

            stationRepository.saveAll(stations);
            log.info("{} estaciones cargadas.", stations.size());
        }
    }
}