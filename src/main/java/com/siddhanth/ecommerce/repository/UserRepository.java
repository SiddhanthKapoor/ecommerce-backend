package com.siddhanth.ecommerce.repository;

import com.siddhanth.ecommerce.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
