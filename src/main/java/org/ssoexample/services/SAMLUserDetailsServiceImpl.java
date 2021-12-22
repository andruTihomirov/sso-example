package org.ssoexample.services;

import org.ssoexample.models.User;
import org.ssoexample.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

  @Autowired
  private UsersRepository usersRepository;

  @Override
  public User loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
    User user = usersRepository.findByEmail(credential.getNameID().getValue());

    /*
     * Anonymous role added to handle unauthorized users trying to login
     * AND Spring security needs role in ROLE_* format
     */

    return user;
  }

  private List<GrantedAuthority> getAuthorities(String role) {
    List<GrantedAuthority> list = new ArrayList<>();
    list.add(new SimpleGrantedAuthority(role));
    return list;
  }
}
