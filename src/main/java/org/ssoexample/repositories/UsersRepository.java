package org.ssoexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ssoexample.models.User;

public interface UsersRepository extends JpaRepository<User, Integer> {

    User findByEmail(final String email);

}
