package com.osms.config;

import com.osms.model.SessionUser;
import com.osms.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class RoleInterceptor implements HandlerInterceptor {

    private final String requiredRole;
    private final UserRepository userRepository;

    public RoleInterceptor(String requiredRole, UserRepository userRepository) {
        this.requiredRole = requiredRole;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        // An account disabled by the admin loses access immediately
        if (!userRepository.isActive(user.getUserId())) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!requiredRole.equals(user.getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
