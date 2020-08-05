package com.mindhub.salvo;


import org.hibernate.validator.constraints.URL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.persistence.Id;
import java.util.List;


@RepositoryRestResource
public interface PlayerRepository extends JpaRepository<Player,Long> {

    Player findByUserName(String username);
}



