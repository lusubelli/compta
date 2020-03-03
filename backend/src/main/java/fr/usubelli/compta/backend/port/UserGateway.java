package fr.usubelli.compta.backend.port;

import fr.usubelli.compta.backend.dto.User;
import fr.usubelli.compta.backend.usecase.UserAlreadyExistsException;

public interface UserGateway {

    User createUser(User user) throws UserAlreadyExistsException;

    User findUser(String email);

    User updateUser(User user);

}
