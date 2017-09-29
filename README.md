# Demo Project to reproduce spring ldap issue #430

Issue: https://github.com/spring-projects/spring-ldap/issues/430
Merge Request: https://github.com/spring-projects/spring-ldap/pull/432

Big Thanks to https://github.com/zivis and https://github.com/derTobsch

## What causes the bug

* Groupsearch is used with credentials
* `DefaultTlsDirContextAuthenticationStrategy` is used for starttls

No anonymous search is allowed in the LDAP image.

## With DefaultTlsDirContextAuthenticationStrategy

```bash
59ceb606 conn=1000 fd=15 ACCEPT from IP=172.17.0.1:33378 (IP=0.0.0.0:389)
59ceb606 conn=1000 op=0 EXT oid=1.3.6.1.4.1.1466.20037
59ceb606 conn=1000 op=0 STARTTLS
59ceb606 conn=1000 op=0 RESULT oid= err=0 text=
TLS: gnutls_certificate_verify_peers2 failed -49
59ceb606 conn=1000 fd=15 TLS established tls_ssf=256 ssf=256
59ceb606 conn=1000 op=1 BIND dn="cn=admin,dc=example,dc=org" method=128
59ceb606 conn=1000 op=1 BIND dn="cn=admin,dc=example,dc=org" mech=SIMPLE ssf=0
59ceb606 conn=1000 op=1 RESULT tag=97 err=0 text=
59ceb606 conn=1000 op=2 SRCH base="ou=People,dc=example,dc=org" scope=2 deref=3 filter="(cn=user)"
59ceb606 <= bdb_equality_candidates: (cn) not indexed
59ceb606 conn=1000 op=2 SEARCH RESULT tag=101 err=0 nentries=1 text=
59ceb606 conn=1000 op=3 UNBIND
59ceb606 conn=1000 fd=15 closed
59ceb606 conn=1001 fd=15 ACCEPT from IP=172.17.0.1:33380 (IP=0.0.0.0:389)
59ceb606 conn=1001 op=0 EXT oid=1.3.6.1.4.1.1466.20037
59ceb606 conn=1001 op=0 STARTTLS
59ceb606 conn=1001 op=0 RESULT oid= err=0 text=
TLS: gnutls_certificate_verify_peers2 failed -49
59ceb606 conn=1001 fd=15 TLS established tls_ssf=256 ssf=256
59ceb606 conn=1001 fd=15 closed (connection lost)
59ceb606 conn=1002 fd=15 ACCEPT from IP=172.17.0.1:33382 (IP=0.0.0.0:389)
59ceb606 conn=1002 op=0 EXT oid=1.3.6.1.4.1.1466.20037
59ceb606 conn=1002 op=0 STARTTLS
59ceb606 conn=1002 op=0 RESULT oid= err=0 text=
TLS: gnutls_certificate_verify_peers2 failed -49
59ceb606 conn=1002 fd=15 TLS established tls_ssf=256 ssf=256
59ceb606 conn=1002 op=1 BIND dn="cn=admin,dc=example,dc=org" method=128
59ceb606 conn=1002 op=1 BIND dn="cn=admin,dc=example,dc=org" mech=SIMPLE ssf=0
59ceb606 conn=1002 op=1 RESULT tag=97 err=0 text=
59ceb606 conn=1002 op=2 SRCH base="ou=Groups,dc=example,dc=org" scope=1 deref=3 filter="(member=cn=user,ou=people,dc=example,dc=org)"
59ceb606 conn=1002 op=2 SRCH attr=cn objectClass javaSerializedData javaClassName javaFactory javaCodeBase javaReferenceAddress javaClassNames javaRemoteLocation
59ceb606 <= bdb_equality_candidates: (member) not indexed
59ceb606 conn=1002 op=2 SEARCH RESULT tag=101 err=0 nentries=1 text=
59ceb606 conn=1002 op=3 UNBIND
59ceb606 conn=1002 fd=15 closed
```

Notice that there is no attempt to bind as `cn=user,ou=people,dc=example,dc=org`!

## With FixDefaultTlsDirContextAuthenticationStrategy

Start a TLS connection and a lookup if the user `cn=user` is available.

```bash
59ceb6c3 conn=1003 fd=15 ACCEPT from IP=172.17.0.1:33384 (IP=0.0.0.0:389)
59ceb6c3 conn=1003 op=0 EXT oid=1.3.6.1.4.1.1466.20037
59ceb6c3 conn=1003 op=0 STARTTLS
59ceb6c3 conn=1003 op=0 RESULT oid= err=0 text=
TLS: gnutls_certificate_verify_peers2 failed -49
59ceb6c3 conn=1003 fd=15 TLS established tls_ssf=256 ssf=256
59ceb6c3 conn=1003 op=1 BIND dn="cn=admin,dc=example,dc=org" method=128
59ceb6c3 conn=1003 op=1 BIND dn="cn=admin,dc=example,dc=org" mech=SIMPLE ssf=0
59ceb6c3 conn=1003 op=1 RESULT tag=97 err=0 text=
59ceb6c3 conn=1003 op=2 SRCH base="ou=People,dc=example,dc=org" scope=2 deref=3 filter="(cn=user)"
59ceb6c3 <= bdb_equality_candidates: (cn) not indexed
59ceb6c3 conn=1003 op=2 SEARCH RESULT tag=101 err=0 nentries=1 text=
59ceb6c3 conn=1003 op=3 UNBIND
59ceb6c3 conn=1003 fd=15 closed
59ceb6c3 conn=1004 fd=15 ACCEPT from IP=172.17.0.1:33386 (IP=0.0.0.0:389)
59ceb6c3 conn=1004 op=0 EXT oid=1.3.6.1.4.1.1466.20037
59ceb6c3 conn=1004 op=0 STARTTLS
59ceb6c3 conn=1004 op=0 RESULT oid= err=0 text=
TLS: gnutls_certificate_verify_peers2 failed -49
59ceb6c3 conn=1004 fd=15 TLS established tls_ssf=256 ssf=256
59ceb6c3 conn=1004 op=1 BIND dn="cn=user,ou=People,dc=example,dc=org" method=128
59ceb6c3 conn=1004 op=1 RESULT tag=97 err=49 text=
59ceb6c3 conn=1004 fd=15 closed (connection lost)
```

The bind with user credentials looks like this: `59ceb6c3 conn=1004 op=1 BIND dn="cn=user,ou=People,dc=example,dc=org" method=128`; this is the actual authentication and the user with wrong credentials can not log in

## To Reproduce

1. Go into the `docker/` directory and build the docker image with the name `openldap`

```bash
docker build -t openldap .
```

2. Start the docker container

```bash
docker run --rm -p 389:389 openldap
```

3. Start the example application

```bash
mvn spring-boot:run
```

4. Visit <http://localhost:8080/> and log in with username `user` and any random password

##### Additional

To test the proposed fix, edit AuthenticationManagerConfiguration.java to instantiate FixDefaultTlsDirContextAuthenticationStrategy instead of DefaultTlsDirContextAuthenticationStrategy.
