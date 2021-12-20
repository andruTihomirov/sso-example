package org.ssoexample.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ssoexample.models.User;

@RestController
@RequestMapping("/")
public class HomeController {

  @GetMapping
  public ResponseEntity<?> submitRequest(ExpiringUsernameAuthenticationToken userToken) {
    try {
      return new ResponseEntity<>(userToken.getPrincipal(), HttpStatus.CREATED);
    } catch(UsernameNotFoundException ex) {
      return new ResponseEntity<>("User does not exists", HttpStatus.UNAUTHORIZED);
    }
  }

  @GetMapping("api/user")
  public ResponseEntity<?> submitRequest2(ExpiringUsernameAuthenticationToken userToken) {
    User user = (User) userToken.getPrincipal();
    return new ResponseEntity<>(user, HttpStatus.CREATED);
  }
}
