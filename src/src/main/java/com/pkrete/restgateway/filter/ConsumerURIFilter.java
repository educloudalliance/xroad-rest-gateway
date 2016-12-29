package com.pkrete.restgateway.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter is responsible of filtering each request to "Consumer/*" URI. The
 * part after "Consumer/" is removed and stored in an attribute. The request is
 * then redirected to ConsumerEndpoint servlet that takes cares of processing
 * it.
 *
 * @author Petteri Kivimäki
 */
public class ConsumerURIFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerURIFilter.class);

    @Override
    public void init(FilterConfig fc) throws ServletException {
        logger.info("Consumer URI filter initialized.");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String servletName = "Consumer";
        String oldURI = request.getRequestURI().substring(request.getContextPath().length() + 1);
        logger.debug("Incoming request : \"{}\"", oldURI);

        if (oldURI.length() > servletName.length()) {
            String resourcePath = oldURI.substring(oldURI.indexOf('/'));
            if (!"/".equals(resourcePath)) {
                // Path must end with "/"
                if (!resourcePath.endsWith("/")) {
                    resourcePath += "/";
                }
                logger.debug("Resource path : \"{}\"", resourcePath);
                request.setAttribute("resourcePath", resourcePath);
            } else {
                logger.trace("Found resource path \"{}\" is not valid.", resourcePath);
            }
        } else {
            logger.trace("No resource path found.");
        }
        req.getRequestDispatcher("Consumer").forward(req, res);
    }

    @Override
    public void destroy() {
        // Nothing to do here.
    }
}
