package com.salemanager.common.config;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class PageSizeLimitFilter extends OncePerRequestFilter {

    private final PaginationProperties paginationProperties;

    public PageSizeLimitFilter(PaginationProperties paginationProperties) {
        this.paginationProperties = paginationProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String pageSize = request.getParameter("pageSize");
        int max = paginationProperties.getMaxPageSize();
        if (pageSize != null) {
            try {
                int size = Integer.parseInt(pageSize);
                if (size > max) {
                    request = new PageSizeRequestWrapper(request, String.valueOf(max));
                }
            } catch (NumberFormatException ignored) {
                // invalid, let controller handle with default
            }
        }
        filterChain.doFilter(request, response);
    }

    private static class PageSizeRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String[]> modifiedParams = new HashMap<>();

        public PageSizeRequestWrapper(HttpServletRequest request, String cappedPageSize) {
            super(request);
            modifiedParams.putAll(request.getParameterMap());
            modifiedParams.put("pageSize", new String[]{cappedPageSize});
        }

        @Override
        public String getParameter(String name) {
            String[] values = modifiedParams.get(name);
            return values != null && values.length > 0 ? values[0] : null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return modifiedParams;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(modifiedParams.keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            return modifiedParams.get(name);
        }
    }
}
