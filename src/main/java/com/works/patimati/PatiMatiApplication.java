package com.works.patimati;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling EKLENDİ (Faz 2 revize blueprint §4/§5): PotentialMatchService'in
// bildirim yeniden-deneme süpürücüsü ve ExternalIngestionMaintenanceJob'un
// takılı-kalmış-gönderi süpürücüleri @Scheduled metotlardır.
@SpringBootApplication
@EnableScheduling
public class PatiMatiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatiMatiApplication.class, args);
    }
}
