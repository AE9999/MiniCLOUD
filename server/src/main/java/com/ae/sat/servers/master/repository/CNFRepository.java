package com.ae.sat.servers.master.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ae.sat.model.Formula;

/**
 * Created by ae on 7-10-16.
 */
public interface CNFRepository extends MongoRepository<Formula, String>  {
}
