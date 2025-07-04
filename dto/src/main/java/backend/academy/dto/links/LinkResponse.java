package backend.academy.dto.links;

import jakarta.validation.constraints.Pattern;

public record LinkResponse(
        long id,
        @Pattern(
                        regexp =
                                "(https?://(www\\.)?(github\\.com/[A-Za-z0-9_-]+/[A-Za-z0-9_-]+|stackoverflow\\.com/questions/[0-9]+/\\S+))",
                        message = "link must be like github repository or stackoverflow question")
                String url,
        String[] tags,
        String[] filters) {}
