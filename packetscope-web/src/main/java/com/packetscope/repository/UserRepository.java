//package com.packetscope.repository;
//
//import com.packetscope.model.User;
//import org.springframework.data.jdbc.repository.query.Query;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.query.Param;
//import java.util.Optional;
//
//// Use CrudRepository instead of JpaRepository
//public interface UserRepository extends CrudRepository<User, Integer> {
//
//    // You can write the raw SQL query directly here!
//    @Query("SELECT * FROM users WHERE username = :username")
//    Optional<User> findByUsername(@Param("username") String username);
//}
