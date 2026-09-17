package schultz.thomas.discord.bot.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class InternalSecretFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalSecretFilter.class);
    private static final String HEADER = "X-Internal-Secret";

    @Value("${schub.internal-secret}")
    private String internalSecret;

    /**
     * La sonde de santé reste joignable sans secret.
     *
     * <p>Sans cette exemption, le filtre rejette {@code /actuator/health} avant que le
     * {@code permitAll} de la configuration de sécurité ne s'applique : Docker ne peut alors
     * jamais déclarer le service sain, et tout ce qui l'attend reste à quai. La sonde n'expose
     * que le statut — le détail est masqué par défaut.</p>
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String provided = request.getHeader(HEADER);

        if (matches(provided)) {
            // Authentifie la requête comme venant du BFF
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "bff-back", null, List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } else {
            log.warn("[InternalSecretFilter] Requête rejetée depuis {} - secret invalide ou absent", request.getRemoteAddr());
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * Comparaison à temps constant : {@code equals} sort au premier octet différent, et la
     * durée de réponse fuit alors la longueur du préfixe correct — de quoi reconstituer le
     * secret octet par octet. Même comparaison que dans les connecteurs (plan §5).
     */
    private boolean matches(String provided) {
        if (provided == null) {
            return false;
        }
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                internalSecret.getBytes(StandardCharsets.UTF_8));
    }
}
