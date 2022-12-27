package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
        private val userRepository: UserRepository,
        private val userService: UserService
) {

    @AfterEach
    fun clean() {
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("회원가입 성공")
    fun saveUserTest() {
        // given
        val request = UserCreateRequest("홍길동", null)

        // when
        userService.saveUser(request)

        // then
        val results = userRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("홍길동")
        assertThat(results[0].age).isNull()
    }

    @Test
    @DisplayName("유저 조회 성공")
    fun getUsersTest() {
        // given
        userRepository.saveAll(listOf(
                User("홍길동A", 20),
                User("홍길동B", null)
        ))

        // when
        val results = userService.users

        // then
        assertThat(results).hasSize(2)
        assertThat(results).extracting("name")
                .containsExactlyInAnyOrder("홍길동B", "홍길동A")
        assertThat(results).extracting("age")
                .containsExactlyInAnyOrder(null, 20)
    }

    @Test
    @DisplayName("회원정보 수정 성공")
    fun updateUserNameTest() {
        // given
        val user = userRepository.save(User("홍길동A", null))
        val request = UserUpdateRequest(user.id, "홍길동B")

        // when
        userService.updateUserName(request)

        // then
        val result = userRepository.findAll()[0]
        assertThat(result.name).isEqualTo("홍길동B")
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    fun deleteUserTest() {
        // given
        userRepository.save(User("홍길동A", null))

        // when
        userService.deleteUser("홍길동A")

        // then
        assertThat(userRepository.findAll()).isEmpty()
    }
}