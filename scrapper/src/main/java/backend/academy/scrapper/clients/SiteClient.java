package backend.academy.scrapper.clients;

public interface SiteClient {
    <T> T get(String uri, Class<T> clazz);

    <T> T getNoTimer(String uri, Class<T> clazz);
}
