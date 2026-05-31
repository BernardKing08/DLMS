package com.dlms.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dlms.auth.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
    
    Optional<Role> findByRoleName(String rolename);
    
}
