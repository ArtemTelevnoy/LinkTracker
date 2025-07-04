package backend.academy.scrapper.controllers;

import backend.academy.scrapper.repositories.filter.FilterRepository;
import backend.academy.scrapper.repositories.tag.TagRepository;
import backend.academy.scrapper.repositories.user.UserRepository;
import backend.academy.scrapper.repositories.userLink.UserLinkRepository;
import backend.academy.scrapper.service.IpRateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/tg-chat")
public class ChatController {
    private final UserRepository userRepository;
    private final UserLinkRepository userLinkRepository;
    private final FilterRepository filterRepository;
    private final TagRepository tagRepository;
    private final IpRateLimiterService ipRateLimiterService;

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String start(@PathVariable long id, HttpServletRequest addr) {
        return ipRateLimiterService.startEndpoint(addr.getRemoteAddr(), () -> {
            userRepository.add(id);
            return "Successful registration";
        });
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String delete(@PathVariable long id, HttpServletRequest addr) {
        return ipRateLimiterService.deleteEndpoint(addr.getRemoteAddr(), () -> {
            filterRepository.deleteAllUserData(id);
            tagRepository.deleteAllUserData(id);
            userLinkRepository.deleteAllUserData(id);
            userRepository.delete(id);
            return "Successful deleting";
        });
    }
}
