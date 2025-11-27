package fr.benseddik.backend.security;

import fr.benseddik.backend.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Implémentation de UserDetails pour Spring Security.
 * Encapsule l'entité User pour l'authentification.
 */
@RequiredArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Peut être utilisé pour vérifier si l'email est vérifié
        return true;
    }

    /**
     * Récupère l'ID de l'utilisateur.
     */
    public UUID getId() {
        return user.getId();
    }

    /**
     * Récupère l'email de l'utilisateur.
     */
    public String getEmail() {
        return user.getEmail();
    }
}