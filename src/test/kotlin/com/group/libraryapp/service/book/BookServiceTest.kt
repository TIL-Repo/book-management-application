package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
        private val bookService: BookService,
        private val bookRepository: BookRepository,
        private val userRepository: UserRepository,
        private val userLoanHistoryRepository: UserLoanHistoryRepository
) {

    @AfterEach
    fun clean() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("책 등록 성공")
    fun saveBookTest() {
        // given
        val request = BookRequest("책A", BookType.COMPUTER)

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo("책A")
        assertThat(books[0].type).isEqualTo(BookType.COMPUTER)
    }

    @Test
    @DisplayName("책 대출 성공")
    fun loanBookTest() {
        // given
        bookRepository.save(Book.fixture("책A"))
        val user = userRepository.save(User("홍길동", null))
        val request = BookLoanRequest("홍길동", "책A")

        // when
        bookService.loanBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo("책A")
        assertThat(results[0].user.id).isEqualTo(user.id)
        assertThat(results[0].isReturn).isFalse
    }

    @Test
    @DisplayName("책 대출 실패")
    fun loanBookFailTest() {
        // given
        bookRepository.save(Book.fixture("책A"))
        val user = userRepository.save(User("홍길동", null))
        userLoanHistoryRepository.save(UserLoanHistory(user, "책A", false))
        val request = BookLoanRequest("홍길동", "책A")

        // when & then
        val message = assertThrows<java.lang.IllegalArgumentException> {
            bookService.loanBook(request)
        }.message

        assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")
    }

    @Test
    @DisplayName("책 반납 성공")
    fun returnBookTest() {
        // given
        bookRepository.save(Book.fixture("책A"))
        val user = userRepository.save(User("홍길동", null))
        userLoanHistoryRepository.save(UserLoanHistory(user, "책A", false))
        val request = BookReturnRequest("홍길동", "책A")

        // when
        bookService.returnBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].isReturn).isTrue
    }
}