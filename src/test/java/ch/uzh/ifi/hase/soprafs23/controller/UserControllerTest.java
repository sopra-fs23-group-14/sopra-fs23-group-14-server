package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  public void getUser_validInput_gotUser() throws Exception{
      long userId = 1L;
      User user = new User();
      user.setName("Firstname Lastname");
      user.setUsername("firstname@lastname");
      user.setStatus(UserStatus.OFFLINE);
      user.setId(1L);
      Optional<User> optUser = Optional.of(user);

      given(userService.getUser(userId)).willReturn(optUser);

      MockHttpServletRequestBuilder getRequest =
              get("/users/" + userId);

      mockMvc.perform(getRequest)
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id", is(user.getId().intValue())))
              .andExpect(jsonPath("$.name", is(user.getName())))
              .andExpect(jsonPath("$.username", is(user.getUsername())))
              .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

    @Test
    public void getUser_invalidInput_NotFound() throws Exception{
        long userId = 1L;
        Optional<User> optUser = Optional.empty();

        given(userService.getUser(userId)).willReturn(optUser);

        MockHttpServletRequestBuilder getRequest =
                get("/users/" + userId);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUser_validInput_updatedUser() throws Exception{
        long userId = 1L;
        User user = new User();
        user.setName("Firstname Lastname");
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);

        willDoNothing().given(userService).updateUser(user);

        MockHttpServletRequestBuilder putRequest =
                put("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user));

        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.id", is((int)userId)))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    @Test
    public void updateUser_invalidInput_notFound() throws Exception{
        long userId = 1L;
        User user = new User();
        user.setName("Firstname Lastname");
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found")).when(userService).updateUser(Mockito.any());
        //willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).given(this.userService).updateUser(user);
        //given(userService.getUser(1L)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        //when(userService).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        MockHttpServletRequestBuilder putRequest =
                put("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user));

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());
    }

  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is(user.getName())))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    //userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}