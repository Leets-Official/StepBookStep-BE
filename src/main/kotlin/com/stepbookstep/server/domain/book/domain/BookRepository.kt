package com.stepbookstep.server.domain.book.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BookRepository : JpaRepository<Book, Long> {

    fun findByIsbn13(isbn13: String): Book?

    @Query(
        value = "SELECT * FROM books WHERE level = :level ORDER BY RAND() LIMIT 4",
        nativeQuery = true
    )
    fun findRandomByLevel(@Param("level") level: Int): List<Book>

    @Query("""
        SELECT b FROM Book b
        WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.publisher LIKE %:keyword%
    """)
    fun searchByKeyword(@Param("keyword") keyword: String): List<Book>

    @Query(
        value = "SELECT * FROM books WHERE genre = :genre ORDER BY RAND() LIMIT 20",
        nativeQuery = true
    )
    fun findRandomByGenre(@Param("genre") genre: String): List<Book>

    @Query(
        value = "SELECT * FROM books WHERE item_page < 200 ORDER BY RAND() LIMIT 20",
        nativeQuery = true
    )
    fun findUnder200Pages(): List<Book>

    @Query(
        value = "SELECT * FROM books WHERE is_bestseller = 1 ORDER BY RAND() LIMIT 20",
        nativeQuery = true
    )
    fun findBestsellers(): List<Book>
}
