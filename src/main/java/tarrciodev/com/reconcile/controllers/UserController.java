package tarrciodev.com.reconcile.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tarrciodev.com.reconcile.DTO.user.UserRequest;
import tarrciodev.com.reconcile.DTO.user.UserResponse;
import tarrciodev.com.reconcile.entities.UserEntity;
import tarrciodev.com.reconcile.services.users.UserService;




@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
   
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest payload){
        var mapUser = new UserEntity();
        mapUser.setName(payload.name());
        mapUser.setEmail(payload.email());
        mapUser.setPassword(payload.password());
        var savedUser = userService.execute(mapUser);
        return ResponseEntity.ok(new UserResponse(savedUser.getName(), savedUser.getEmail()));
    }

    @GetMapping
    public List<UserEntity> getUser(){
        return this.userService.getResults();
    }
    
}
