/**
 * @projectName springAi
 * @package com.example.springai.service
 * @className com.example.springai.service.UserService
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.service;

import com.example.springai.model.User;

public interface UserService {

    boolean addUser(User user);

    User login (User user);

    User getUser(String id);
}
