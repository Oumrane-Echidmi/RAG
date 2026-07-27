package com.example.RAG;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * MVC controller that serves the single-page frontend.
 *
 * <p>Returns the {@code index.html} template for the root path ({@code /}) and
 * the explicit {@code /index.html} path. The frontend is a static HTML page
 * with embedded CSS/JS that communicates with {@code /api/chat}.</p>
 */
@Controller
public class HomeController {

    /**
     * Serves the main application page.
     *
     * <p>Loads {@code templates/index.html} from the classpath and returns it
     * with the {@code text/html} content type so the browser renders it.</p>
     *
     * @return the {@code index.html} resource from the classpath
     */
    @GetMapping(value = {"/", "/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource index() {
        return new ClassPathResource("templates/index.html");
    }
}
