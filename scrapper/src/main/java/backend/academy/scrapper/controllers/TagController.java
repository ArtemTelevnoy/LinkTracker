package backend.academy.scrapper.controllers;

import backend.academy.dto.tags.LinksByTagResponse;
import backend.academy.dto.tags.RemoveLinksByTagResponse;
import backend.academy.dto.tags.TagsResponse;
import backend.academy.scrapper.repositories.link.LinkRepository;
import backend.academy.scrapper.repositories.tag.TagRepository;
import backend.academy.scrapper.service.IpRateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/tags")
public class TagController {
    private final LinkRepository linkRepository;
    private final TagRepository tagRepository;
    private final IpRateLimiterService ipRateLimiterService;

    @GetMapping("/{id}/all")
    @ResponseStatus(HttpStatus.OK)
    public TagsResponse tags(@PathVariable long id, HttpServletRequest addr) {
        return ipRateLimiterService.tagsEndpoint(addr.getRemoteAddr(), () -> {
            final String[] tags = tagRepository.getUserTags(id);
            return new TagsResponse(tags, tags.length);
        });
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LinksByTagResponse getByTag(
            @PathVariable long id, @RequestHeader("Tag") String tag, HttpServletRequest addr) {
        return ipRateLimiterService.getByTagEndpoint(addr.getRemoteAddr(), () -> {
            final List<Long> linksIds = tagRepository.getLinksIds(id, tag);
            final String[] urls = linksIds.stream().map(linkRepository::getUrl).toArray(String[]::new);
            return new LinksByTagResponse(urls, urls.length);
        });
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RemoveLinksByTagResponse deleteByTag(
            @PathVariable long id, @RequestHeader("Tag") String tag, HttpServletRequest addr) {
        return ipRateLimiterService.deleteByTagEndpoint(addr.getRemoteAddr(), () -> {
            final List<Long> linksIds = tagRepository.deleteByTag(id, tag);
            final String[] urls = linksIds.stream().map(linkRepository::getUrl).toArray(String[]::new);
            return new RemoveLinksByTagResponse(urls, urls.length);
        });
    }
}
