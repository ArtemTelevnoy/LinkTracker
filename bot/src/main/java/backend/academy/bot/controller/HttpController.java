package backend.academy.bot.controller;

import backend.academy.bot.service.IpRateLimiterService;
import backend.academy.bot.service.ProcessingUpdates;
import backend.academy.dto.links.LinkUpdate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class HttpController {
    private final ProcessingUpdates processingUpdates;
    private final IpRateLimiterService ipRateLimiterService;

    @PostMapping("/updates")
    @ResponseStatus(HttpStatus.OK)
    public String updates(@RequestBody LinkUpdate update, HttpServletRequest addr) {
        return ipRateLimiterService.updatesEndpoint(addr.getRemoteAddr(), () -> {
            processingUpdates.updates(update);
            return "updated";
        });
    }
}
