package co.edu.eci.taller7.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @PostMapping
    public User createUser(@RequestParam String username, @RequestParam String email) {
        return userService.createUser(username, email);
    }
}