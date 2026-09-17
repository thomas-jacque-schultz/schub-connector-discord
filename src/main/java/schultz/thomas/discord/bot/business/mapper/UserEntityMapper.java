package schultz.thomas.discord.bot.business.mapper;

import org.mapstruct.*;
import schultz.thomas.discord.bot.data.entity.UserEntity;

@Mapper(componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE)
public interface UserEntityMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserEntityFromSource(UserEntity source, @MappingTarget UserEntity target);
}
