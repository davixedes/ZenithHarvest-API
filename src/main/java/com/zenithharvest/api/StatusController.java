package com.zenithharvest.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StatusController {

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
            "app", "ZenithHarvest API",
            "descricao", "Seguro parametrico agricola ativado por satelite",
            "status", "online"
        );
    }

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("status", "ok");
    }
}
