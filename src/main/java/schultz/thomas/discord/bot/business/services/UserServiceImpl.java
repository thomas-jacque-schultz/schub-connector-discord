package schultz.thomas.discord.bot.business.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import schultz.thomas.discord.bot.business.mapper.UserEntityMapper;
import schultz.thomas.discord.bot.data.entity.UserEntity;
import schultz.thomas.discord.bot.data.enums.UserPrivilegeEnum;
import schultz.thomas.discord.bot.data.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private List<UserEntity> userStateCache;

    private final UserRepository userRepository;

    private final UserEntityMapper userEntityMapper;

    @PostConstruct
    public void init() {
        userStateCache = userRepository.findAll();
    }


    public void createUser(UserEntity ue) throws IllegalArgumentException {
        if(userStateCache.stream().anyMatch(user -> ue.getDiscordId().equals(user.getDiscordId()))) {
            throw new IllegalArgumentException("User already exists");
        }
        userStateCache.add(userRepository.save(ue));
    }

    public void updateUser(UserEntity userEntity) {
        UserEntity candidate  =  userStateCache.stream().filter(u -> u.getDiscordId().equals(userEntity.getDiscordId())).findFirst().orElse(null);
        if(candidate == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        userEntityMapper.updateUserEntityFromSource(candidate, userEntity);
        userRepository.save(candidate);
    }

    public UserPrivilegeEnum getRole(String discordId){
        Optional<UserEntity> candidat  =  userStateCache.stream().filter(u -> u.getDiscordId().equals(discordId)).findFirst();
        return candidat.map(UserEntity::getPrivilege).orElse(null);
    }

    public UserEntity get(String discordId) {
        Optional<UserEntity> candidat  =  userStateCache.stream().filter(u -> u.getDiscordId().equals(discordId)).findFirst();
        return candidat.orElse(null);
    }
}
