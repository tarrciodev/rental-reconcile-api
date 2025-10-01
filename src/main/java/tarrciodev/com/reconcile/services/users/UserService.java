package tarrciodev.com.reconcile.services.users;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tarrciodev.com.reconcile.entities.UserEntity;
import tarrciodev.com.reconcile.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    public UserEntity execute(UserEntity payload){
        

        Optional<UserEntity> user = userRepository.findByEmail(payload.getEmail());
        if(user.isPresent()){
            throw new RuntimeException("User already exists");
        }
        var newUser = userRepository.save(payload);
        return newUser;
    }

    public List<UserEntity> getResults(){
        return this.userRepository.findAll();
    }
}
