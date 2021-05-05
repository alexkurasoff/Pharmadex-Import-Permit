package org.msh.pdex.ipermit;



import org.msh.pdex.dto.WorkspaceDTO;
import org.msh.pdex.ipermit.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;



@SpringBootApplication
@PropertySource("classpath:application_test.properties")
@ComponentScan("org.msh.pdex")
@Profile({"test"})
public class PdexImportPermitApplicationTest implements WebMvcConfigurer  {
	
	@Autowired
	UserService userService;
	
	public static void main(String[] args) {
		SpringApplication.run(PdexImportPermitApplicationTest.class, args);
	}
	
	/**
	 * We will use cookie locale resolver, because we are too lazy to create locale field in a user's profile
	 * @return
	 */
	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver clr = new CookieLocaleResolver();
	    clr.setDefaultLocale(WorkspaceDTO.DEFAULT_LOCALE);
	    clr.setCookieName("lang");
        clr.setCookieMaxAge(365*24*60*60); //year
	    return clr;
	}
	
	/**
	 * We will need propose some way to change locale. Let it be GET query with ?lang=locale parameter e.g. http://some.somewhere?lang=en_us
	 * @return
	 */
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
	    LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
	    lci.setParamName("lang");
	    return lci;
	}
	
	/**
	 * Add to the servlet interceptor's chain our locale change intercepter
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
	    registry.addInterceptor(localeChangeInterceptor());
	}
	
	 @Bean
	 public DaoAuthenticationProvider authenticationProvider() {
	     DaoAuthenticationProvider authProvider
	       = new DaoAuthenticationProvider();
	     authProvider.setUserDetailsService(userService);
	     authProvider.setPasswordEncoder(encoder());
	     return authProvider;
	 }
	  
	 @Bean
	 public PasswordEncoder encoder() {
		 return new BCryptPasswordEncoder();
	 }

}
