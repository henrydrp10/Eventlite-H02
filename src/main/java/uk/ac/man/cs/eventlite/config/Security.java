package uk.ac.man.cs.eventlite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class Security extends WebSecurityConfigurerAdapter {

	public static final String ADMIN_ROLE = "ADMINISTRATOR";
	public static final String Event_Organizer_ROLE = "EVENT_ORGANIZER";
	public static final String Event_Seeker_ROLE = "EVENT SEEKEER";
	// List the mappings/methods for which no authorisation is required.
	// By default we allow all GETs and full access to the H2 console.
	private static final RequestMatcher[] NO_AUTH = { new AntPathRequestMatcher("/webjars/**", "GET"),
			 new AntPathRequestMatcher("/h2-console/**"),new AntPathRequestMatcher("/**", "GET")};
	// comment new AntPathRequestMatcher("/**", "GET") for ole base to work
	// for now not applied since tests would need changing
	
	private static final RequestMatcher[] A = { new AntPathRequestMatcher("/events/**", "GET") };
	//new AntPathRequestMatcher("/**", "GET")
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// By default, all requests are authenticated except our specific list.
		http.authorizeRequests().requestMatchers(NO_AUTH).permitAll().anyRequest().hasRole(ADMIN_ROLE);
		http.authorizeRequests().requestMatchers(A).permitAll().anyRequest().hasRole(Event_Seeker_ROLE);
		http.authorizeRequests().requestMatchers(A).permitAll().anyRequest().hasRole(Event_Organizer_ROLE);

		// Use form login/logout for the Web.
		http.formLogin().loginPage("/sign-in").permitAll();
		http.logout().logoutUrl("/sign-out").logoutSuccessUrl("/").permitAll();

		// Use HTTP basic for the API.
		http.requestMatcher(new AntPathRequestMatcher("/api/**")).httpBasic();

		// Only use CSRF for Web requests.
		// Disable CSRF for the API and H2 console.
		http.antMatcher("/**").csrf().ignoringAntMatchers("/api/**", "/h2-console/**");

		// Disable X-Frame-Options for the H2 console.
		http.headers().frameOptions().disable();
	}

	@Override
	@Bean
	public UserDetailsService userDetailsService() {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

		UserDetails rob = User.withUsername("Rob").password(encoder.encode("Haines")).roles(ADMIN_ROLE).build();
		UserDetails caroline = User.withUsername("Caroline").password(encoder.encode("Jay")).roles(ADMIN_ROLE).build();
		UserDetails markel = User.withUsername("Markel").password(encoder.encode("Vigo")).roles(ADMIN_ROLE).build();
		UserDetails joana = User.withUsername("Joana").password(encoder.encode("Cruz")).roles(Event_Seeker_ROLE).build();
		UserDetails josy = User.withUsername("Josy").password(encoder.encode("Friend")).roles(Event_Organizer_ROLE).build();
		UserDetails mustafa = User.withUsername("Mustafa").password(encoder.encode("Mustafa")).roles(Event_Seeker_ROLE)
				.build();

		return new InMemoryUserDetailsManager(rob, caroline, markel, mustafa, joana, josy);
	}
}
