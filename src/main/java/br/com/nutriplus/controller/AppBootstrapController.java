package br.com.nutriplus.controller;

import br.com.nutriplus.dto.response.AppBootstrapResponse;
import br.com.nutriplus.service.AppBootstrapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class AppBootstrapController {

    private final AppBootstrapService appBootstrapService;

    public AppBootstrapController(AppBootstrapService appBootstrapService) {
        this.appBootstrapService = appBootstrapService;
    }

    @GetMapping("/bootstrap")
    public AppBootstrapResponse bootstrap() {
        return appBootstrapService.bootstrap();
    }
}
