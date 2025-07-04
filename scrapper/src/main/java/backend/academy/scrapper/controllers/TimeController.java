package backend.academy.scrapper.controllers;

import backend.academy.dto.chats.TimeBody;
import backend.academy.scrapper.repositories.settings.SettingsRepository;
import backend.academy.scrapper.service.IpRateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/time")
public class TimeController {
    private final SettingsRepository settingsRepository;
    private final IpRateLimiterService ipRateLimiterService;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteTime(@PathVariable long id, HttpServletRequest addr) {
        return ipRateLimiterService.deleteTimeEndpoint(addr.getRemoteAddr(), () -> {
            settingsRepository.delete(id);
            return "Successful deleting";
        });
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String updateTime(@PathVariable long id, @RequestBody TimeBody request, HttpServletRequest addr) {
        return ipRateLimiterService.updateTimeEndpoint(addr.getRemoteAddr(), () -> {
            settingsRepository.add(id, request);
            return "Successful updating";
        });
    }
}
