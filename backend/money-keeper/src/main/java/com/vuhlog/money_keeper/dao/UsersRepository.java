package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
    boolean existsByUsername(String username);

    @Query(value = "SELECT * FROM users u WHERE u.email = :email AND u.o_auth2 = :oauth2", nativeQuery = true)
    Users findByEmailAndOAuth2(@Param("email")String email, @Param("oauth2") Boolean oauth2);

    Optional<Users> findByUsername(String username);

    Page<Users> findByUsernameContainsIgnoreCase(String username, Pageable pageable);


}