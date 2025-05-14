package com.example.recipeoop_1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller intended to act as a proxy or fallback for requests
 * made to a specific hardcoded domain.
 * <p>
 * This controller is mapped to the domain "recipe-management-api.onrender.com".
 * Its primary purpose, as indicated by its internal comments, seems to be to catch
 * all requests to this domain and provide a simple string response.
 * </p>
 * <p>
 * Note: The utility of this controller should be reviewed in the context of
 * deployment on Render.com, as Render will assign a dynamic URL to the service
 * (e.g., {@code your-app-name.onrender.com}). If clients are configured to use
 * the Render-assigned URL, this controller, mapped to a specific different subdomain,
 * might not be hit as expected or might be redundant.
 * </p>
 *
 * @author Your Name/Team Name
 * @version 1.0
 * @since 2025-05-14
 */
@RestController
@RequestMapping("recipe-management-api.onrender.com") // Maps requests for this specific host
public class ProxyController {
    // Original comment: This controller just catches requests to the hard-coded API URL
    // and forwards them to your actual controllers (Note: current implementation returns a string, doesn't forward)

    /**
     * Handles all incoming requests (/**) to the mapped host
     * ("recipe-management-api.onrender.com").
     * <p>
     * This method acts as a catch-all for any path under the configured domain.
     * It returns a simple string message indicating that the proxy is working.
     * According to the original inline comment, this response is intended as a fallback
     * and ideally should not be seen by end-users if the main application routing is correct.
     * </p>
     * HTTP Method: Any (due to {@code @RequestMapping("/**")})
     * Path: Any path under "recipe-management-api.onrender.com"
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: Always returns a plain string: "API proxy is working. This is a fallback and shouldn't be seen."</li>
     * </ul>
     *
     * @return A string message indicating the proxy is active.
     */
    @RequestMapping("/**")
    public String handleProxy() {
        return "API proxy is working. This is a fallback and shouldn't be seen.";
    }
}