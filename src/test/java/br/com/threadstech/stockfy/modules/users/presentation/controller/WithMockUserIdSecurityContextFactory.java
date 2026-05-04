package br.com.threadstech.stockfy.modules.users.presentation.controller;

import java.util.Arrays;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockUserIdSecurityContextFactory implements WithSecurityContextFactory<WithMockUserId> {

	@Override
	public SecurityContext createSecurityContext(WithMockUserId annotation) {
		var authorities = Arrays.stream(annotation.roles())
			.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
			.map(SimpleGrantedAuthority::new)
			.toList();

		var authentication = new UsernamePasswordAuthenticationToken(UUID.fromString(annotation.id()), null,
				authorities);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		return context;
	}

}
