package com.tattoo.scheduler.mapper;

import com.tattoo.scheduler.domain.User;
import com.tattoo.scheduler.model.UserEntity;
import com.tattoo.scheduler.util.TestData;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserMapperTest {
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toDomain_shouldMapEntityToDomain() {

        UserEntity entity = TestData.createUserEntityWithId(1L);

        User domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getName()).isEqualTo(entity.getName());
        assertThat(domain.getEmail()).isEqualTo(entity.getEmail());
        assertThat(domain.getPhoneNumber()).isEqualTo(entity.getPhoneNumber());
        assertThat(domain.getPassword()).isEqualTo(entity.getPassword());
        assertThat(domain.getBirthDate()).isEqualTo(entity.getBirthDate());
        assertThat(domain.getCreatedAt()).isEqualTo(entity.getCreatedAt());
    }

    @Test
    void toEntity_shouldMapDomainToEntity() {

        User domain = TestData.createTestUserDomain();

        UserEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getName()).isEqualTo(domain.getName());
        assertThat(entity.getEmail()).isEqualTo(domain.getEmail());
        assertThat(entity.getPhoneNumber()).isEqualTo(domain.getPhoneNumber());
        assertThat(entity.getPassword()).isEqualTo(domain.getPassword());
        assertThat(entity.getBirthDate()).isEqualTo(domain.getBirthDate());
        assertThat(entity.getCreatedAt()).isEqualTo(domain.getCreatedAt());
        assertThat(entity.getBookingEntities()).isNull();
    }
}
