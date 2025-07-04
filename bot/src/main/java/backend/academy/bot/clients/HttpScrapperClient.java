package backend.academy.bot.clients;

import backend.academy.bot.configs.BotConfig;
import backend.academy.bot.exceptions.BadRequestException;
import backend.academy.bot.exceptions.NotFoundException;
import backend.academy.dto.api.ApiErrorResponse;
import backend.academy.dto.api.NotSupportedErrorCodeException;
import backend.academy.dto.api.SupportedErrorCodeException;
import backend.academy.dto.chats.TimeBody;
import backend.academy.dto.links.*;
import backend.academy.dto.tags.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class HttpScrapperClient implements ScrapperClient {
    private final WebClient client;
    private final ClientUtils clientUtils;
    private final Duration timeout;

    @Autowired
    public HttpScrapperClient(BotConfig config, ClientUtils clientUtils) {
        client = WebClient.create(config.scrapperBaseUrl());
        this.clientUtils = clientUtils;
        timeout = Duration.ofMillis(config.timeOutDuration());
    }

    @Override
    @Retry(name = "webClientRetry")
    @CircuitBreaker(name = "webClientCircuitBreaker")
    public LinkResponse getUntrackResponse(long id, String url) {
        return client.method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(id))
                .bodyValue(new RemoveLinkRequest(url))
                .retrieve()
                .onStatus(clientUtils::isSupportHttp, HttpScrapperClient::supportedErrorCode)
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> {
                    log.warn("User {} wasn't untrack link", id);
                    return badRequest(clientResponse);
                })
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> {
                    log.warn("Link {} wasn't tracked by user {}", url, id);
                    return clientResponse
                            .bodyToMono(ApiErrorResponse.class)
                            .flatMap(error -> Mono.error(new NotFoundException(error)));
                })
                .onStatus(code -> !HttpStatus.OK.equals(code), HttpScrapperClient::notSupportedErrorCode)
                .bodyToMono(LinkResponse.class)
                .timeout(timeout)
                .block();
    }

    @Override
    @Retry(name = "webClientRetry")
    @CircuitBreaker(name = "webClientCircuitBreaker")
    public LinkResponse getTrackResponse(long id, String url, String[] tags, String[] filters) {
        return client.post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(id))
                .bodyValue(new AddLinkRequest(url, tags, filters))
                .retrieve()
                .onStatus(clientUtils::isSupportHttp, HttpScrapperClient::supportedErrorCode)
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> {
                    log.warn("User {} wasn't track link", id);
                    return badRequest(clientResponse);
                })
                .onStatus(code -> !HttpStatus.OK.equals(code), HttpScrapperClient::notSupportedErrorCode)
                .bodyToMono(LinkResponse.class)
                .timeout(timeout)
                .block();
    }

    @Override
    @Retry(name = "webClientRetry")
    @CircuitBreaker(name = "webClientCircuitBreaker")
    public String getStartResponse(long id) {
        return client.post()
                .uri("/tg-chat/{id}", id)
                .retrieve()
                .onStatus(clientUtils::isSupportHttp, HttpScrapperClient::supportedErrorCode)
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> {
                    log.warn("User {} wasn't registered", id);
                    return badRequest(clientResponse);
                })
                .onStatus(code -> !HttpStatus.OK.equals(code), HttpScrapperClient::notSupportedErrorCode)
                .bodyToMono(String.class)
                .timeout(timeout)
                .block();
    }

    @Override
    @Retry(name = "webClientRetry")
    @CircuitBreaker(name = "webClientCircuitBreaker")
    public ListLinksResponse getListResponse(long id) {
        return client.get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(id))
                .retrieve()
                .onStatus(clientUtils::isSupportHttp, HttpScrapperClient::supportedErrorCode)
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> {
                    log.warn("User {} wasn't got list of command", id);
                    return badRequest(clientResponse);
                })
                .onStatus(code -> !HttpStatus.OK.equals(code), HttpScrapperClient::notSupportedErrorCode)
                .bodyToMono(ListLinksResponse.class)
                .timeout(timeout)
                .block();
    }

    @Override
    @Retry(name = "webClientRetry")
    @CircuitBreaker(name = "webClientCircuitBreaker")
    public TagsResponse getTagsResponse(long id) {
        return client.get()
                .uri("/tags/{id}/all", String.valueOf(id))
                .retrieve()
                .onStatus(clientUtils::isSupportHttp, HttpScrapperClient::supportedErrorCode)
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> {
                    log.warn("User {} wasn't got tags of command", id);
                    return badRequest(clientResponse);
                })
                .onStatus(code -> !HttpStatus.OK.equals(code), HttpScrapperClient::notSupportedErrorCode)
                .bodyToMono(TagsResponse.class)
                .timeout(timeout)
                .block();
    }

    @Override
    @Retry(name = "webClientRetry")
    @CircuitBreaker(name = "webClientCircuitBreaker")
    public LinksByTagResponse getByTagResponse(long id, String tag) {
        return client.get()
                .uri("/tags/{id}", String.valueOf(id))
                .header("Tag", tag)
                .retrieve()
                .onStatus(clientUtils::isSupportHttp, HttpScrapperClient::supportedErrorCode)
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> {
                    log.warn("User {} wasn't got getByTag command for tag {}", id, tag);
                    return badRequest(clientResponse);
                })
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> notFoundTag(clientResponse, id, tag))
                .onStatus(code -> !HttpStatus.OK.equals(code), HttpScrapperClient::notSupportedErrorCode)
                .bodyToMono(LinksByTagResponse.class)
                .timeout(timeout)
                .block();
    }

    @Override
    @Retry(name = "webClientRetry")
    @CircuitBreaker(name = "webClientCircuitBreaker")
    public RemoveLinksByTagResponse removeByTagResponse(long id, String tag) {
        return client.delete()
                .uri("/tags/{id}", String.valueOf(id))
                .header("Tag", tag)
                .retrieve()
                .onStatus(clientUtils::isSupportHttp, HttpScrapperClient::supportedErrorCode)
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> {
                    log.warn("User {} wasn't got removeByTag for tag {}", id, tag);
                    return badRequest(clientResponse);
                })
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> notFoundTag(clientResponse, id, tag))
                .onStatus(code -> !HttpStatus.OK.equals(code), HttpScrapperClient::notSupportedErrorCode)
                .bodyToMono(RemoveLinksByTagResponse.class)
                .timeout(timeout)
                .block();
    }

    @Override
    @Retry(name = "webClientRetry")
    @CircuitBreaker(name = "webClientCircuitBreaker")
    public String getChangeTimeResponse(long id, short hours, short minutes) {
        return client.post()
                .uri("/time/{id}", String.valueOf(id))
                .bodyValue(new TimeBody(hours, minutes))
                .retrieve()
                .onStatus(clientUtils::isSupportHttp, HttpScrapperClient::supportedErrorCode)
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> {
                    log.warn("User {} wasn't changing time", id);
                    return badRequest(clientResponse);
                })
                .onStatus(code -> !HttpStatus.OK.equals(code), HttpScrapperClient::notSupportedErrorCode)
                .bodyToMono(String.class)
                .timeout(timeout)
                .block();
    }

    @Override
    @Retry(name = "webClientRetry")
    @CircuitBreaker(name = "webClientCircuitBreaker")
    public String getChangeTimeResponse(long id) {
        return client.delete()
                .uri("/time/{id}", String.valueOf(id))
                .retrieve()
                .onStatus(clientUtils::isSupportHttp, HttpScrapperClient::supportedErrorCode)
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> {
                    log.warn("User {} wasn't deleting time", id);
                    return badRequest(clientResponse);
                })
                .onStatus(code -> !HttpStatus.OK.equals(code), HttpScrapperClient::notSupportedErrorCode)
                .bodyToMono(String.class)
                .timeout(timeout)
                .block();
    }

    private static Mono<? extends Throwable> notSupportedErrorCode(@NotNull ClientResponse clientResponse) {
        log.error("{} error code response, not supported", clientResponse.statusCode());
        return Mono.error(new NotSupportedErrorCodeException(clientResponse.statusCode()));
    }

    private static Mono<? extends Throwable> supportedErrorCode(@NotNull ClientResponse clientResponse) {
        log.error("{} supported error code response", clientResponse.statusCode());
        return Mono.error(new SupportedErrorCodeException(clientResponse.statusCode()));
    }

    private static Mono<? extends Throwable> notFoundTag(@NotNull ClientResponse response, long id, String tag) {
        log.warn("User {} doesn't have tag {}", id, tag);
        return response.bodyToMono(ApiErrorResponse.class).flatMap(error -> Mono.error(new NotFoundException(error)));
    }

    private static Mono<? extends Throwable> badRequest(@NotNull ClientResponse response) {
        return response.bodyToMono(ApiErrorResponse.class).flatMap(error -> Mono.error(new BadRequestException(error)));
    }
}
