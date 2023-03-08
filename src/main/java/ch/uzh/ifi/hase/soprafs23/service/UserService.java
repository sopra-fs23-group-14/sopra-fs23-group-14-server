package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public Optional<User> getUser(Long userID){
      return this.userRepository.findById(userID);
  }

  public void updateUser(User user){
      Optional<User> optUserFromDb = userRepository.findById(user.getId());

      if(optUserFromDb.isEmpty())
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User cannot be updated, id: " + user.getId());

      var userFromDb = optUserFromDb.get();
      userFromDb.setStatus(user.getStatus());
      userFromDb.setUsername(user.getUsername());
      userFromDb.setName(user.getName());

      userRepository.save(userFromDb);
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.OFFLINE);
    newUser.setCreationDate(new Date());
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  public User getUserByUsernameAndName(UserPostDTO user){
      User userByUsername = userRepository.findByUsername(user.getUsername());

      if(userByUsername == null){
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username is not valid");
      }
      if(!userByUsername.getPassword().equals(user.getPassword()))
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is not valid");

      return userByUsername;
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format(baseErrorMessage, "username and the name", "are"));
    } else if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "username", "is"));
    }
  }
}
