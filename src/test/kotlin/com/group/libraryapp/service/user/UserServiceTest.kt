package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
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
        private val userService: UserService,
        private val userLoanHistoryRepository: UserLoanHistoryRepository,
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
        val results = userService.getUsers()

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
        val request = UserUpdateRequest(user.id!!, "홍길동B")

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

    @Test
    @DisplayName("대여 기록이 없는 유저도 응답에 포함")
    fun getUserLoanHistoriesTest() {
        // given
        userRepository.save(User("홍길동A", null))

        // when
        val results = userService.getUserLoanHistories()

        // then
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("홍길동A")
        assertThat(results[0].books).isEmpty()
    }

    @Test
    @DisplayName("대여 기록이 많은 유저의 응답에 정상 동작")
    fun getUserLoanHistoriesTest2() {
        // given
        val user = userRepository.save(User("홍길동A", null))
        userLoanHistoryRepository.saveAll(listOf(
                UserLoanHistory.fixture(user, "책A", UserLoanStatus.LOANED),
                UserLoanHistory.fixture(user, "책B", UserLoanStatus.LOANED),
                UserLoanHistory.fixture(user, "책C", UserLoanStatus.RETURNED)
        ))

        // when
        val results = userService.getUserLoanHistories()

        // then
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("홍길동A")
        assertThat(results[0].books).hasSize(3)
        assertThat(results[0].books).extracting("name")
                .containsExactlyInAnyOrder("책A", "책B", "책C")
        assertThat(results[0].books).extracting("isReturn")
                .containsExactlyInAnyOrder(false, false, true)
    }

    @Test
    @DisplayName("위 두 경우가 합친 케이스")
    fun getUserLoanHistoriesTest3() {
        // given
        val users = userRepository.saveAll(listOf(
                User("홍길동A", null),
                User("홍길동B", null),
        ))
        userLoanHistoryRepository.saveAll(listOf(
                UserLoanHistory.fixture(users[0], "책A", UserLoanStatus.LOANED),
                UserLoanHistory.fixture(users[0], "책B", UserLoanStatus.LOANED),
                UserLoanHistory.fixture(users[0], "책C", UserLoanStatus.RETURNED)
        ))

        // when
        val results = userService.getUserLoanHistories()

        // then
        assertThat(results).hasSize(2)

        val userAResult = results.first { it.name == "홍길동A" }

        assertThat(userAResult.name).isEqualTo("홍길동A")
        assertThat(userAResult.books).hasSize(3)
        assertThat(userAResult.books).extracting("name")
                .containsExactlyInAnyOrder("책A", "책B", "책C")
        assertThat(userAResult.books).extracting("isReturn")
                .containsExactlyInAnyOrder(false, false, true)

        val userBResult = results.first { it.name == "홍길동B" }

        assertThat(userBResult.books).isEmpty()
    }
}