package org.tobsch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.AbstractTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.DefaultTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;


@Configuration
@EnableWebSecurity
public class AuthenticationManagerConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {

        auth.ldapAuthentication()
            .userSearchBase("ou=People")
            .userSearchFilter("cn={0}")
            .groupSearchBase("ou=Groups")
            .groupSearchFilter("member={0}")
            .contextSource(contextSource());
    }


    @Bean
    public LdapContextSource contextSource() {

        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:389");
        contextSource.setBase("dc=example,dc=org");
        AbstractTlsDirContextAuthenticationStrategy strategy = new DefaultTlsDirContextAuthenticationStrategy();
//        AbstractTlsDirContextAuthenticationStrategy strategy = new FixDefaultTlsDirContextAuthenticationStrategy();
        strategy.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        contextSource.setAuthenticationStrategy(strategy);
        contextSource.setUserDn("cn=admin,dc=example,dc=org");
        contextSource.setPassword("admin");

        // This implementation with reconnect works

        return contextSource;
    }
}
