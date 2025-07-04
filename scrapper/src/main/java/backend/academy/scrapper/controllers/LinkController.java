package backend.academy.scrapper.controllers;

import static backend.academy.scrapper.link.LinkType.*;

import backend.academy.dto.links.AddLinkRequest;
import backend.academy.dto.links.LinkResponse;
import backend.academy.dto.links.ListLinksResponse;
import backend.academy.dto.links.RemoveLinkRequest;
import backend.academy.scrapper.link.LinkInfo;
import backend.academy.scrapper.link.LinkType;
import backend.academy.scrapper.repositories.filter.FilterRepository;
import backend.academy.scrapper.repositories.link.LinkRepository;
import backend.academy.scrapper.repositories.tag.TagRepository;
import backend.academy.scrapper.repositories.userLink.UserLinkRepository;
import backend.academy.scrapper.service.GitHubService;
import backend.academy.scrapper.service.IpRateLimiterService;
import backend.academy.scrapper.service.StackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/links")
public class LinkController {
    private final LinkRepository linkRepository;
    private final UserLinkRepository userLinkRepository;
    private final FilterRepository filterRepository;
    private final TagRepository tagRepository;
    private final GitHubService gitHubService;
    private final StackService stackService;
    private final IpRateLimiterService ipRateLimiterService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ListLinksResponse list(@RequestHeader("Tg-Chat-Id") long id, HttpServletRequest addr) {
        return ipRateLimiterService.listEndpoint(addr.getRemoteAddr(), () -> {
            final List<Long> linksIds = userLinkRepository.getUserLinksIds(id);
            final var linkResponses = linksIds.stream()
                    .map(linkId -> new LinkResponse(
                            linkId,
                            linkRepository.getUrl(linkId),
                            tagRepository.get(id, linkId),
                            filterRepository.get(id, linkId)))
                    .toArray(LinkResponse[]::new);

            return new ListLinksResponse(linkResponses, linkResponses.length);
        });
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public LinkResponse track(
            @RequestBody @Valid AddLinkRequest request, @RequestHeader("Tg-Chat-Id") long id, HttpServletRequest addr) {
        return ipRateLimiterService.trackEndpoint(addr.getRemoteAddr(), () -> {
            final LinkType type = request.link().contains("://github") ? GITHUB : STACKOVERFLOW;
            final Instant updateTime = type == GITHUB
                    ? gitHubService.getLastUpdateOrNow(request.link())
                    : stackService.getLastUpdateOrNow(request.link());
            final long linkId = linkRepository.add(new LinkInfo(request.link(), updateTime, type == GITHUB));

            final String[] filters = new HashSet<>(List.of(request.filters())).toArray(String[]::new);
            final String[] tags = new HashSet<>(List.of(request.tags())).toArray(String[]::new);

            userLinkRepository.add(id, linkId);
            filterRepository.add(id, linkId, filters);
            tagRepository.add(id, linkId, tags);

            return new LinkResponse(linkId, request.link(), tags, filters);
        });
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public LinkResponse untrack(
            @RequestBody @Valid RemoveLinkRequest removeLink,
            @RequestHeader("Tg-Chat-Id") long id,
            HttpServletRequest addr) {
        return ipRateLimiterService.untrackEndpoint(addr.getRemoteAddr(), () -> {
            final long linkId = linkRepository.getId(removeLink.link());
            userLinkRepository.delete(id, linkId);
            final LinkResponse response = new LinkResponse(
                    linkId, removeLink.link(), tagRepository.get(id, linkId), filterRepository.get(id, linkId));

            tagRepository.delete(id, linkId);
            filterRepository.delete(id, linkId);
            return response;
        });
    }
}
