package com.stepbookstep.server.domain.book.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BookRepository : JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

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

    @Query("SELECT b FROM Book b WHERE b.genre = :genre")
    fun findAllByGenre(@Param("genre") genre: String): List<Book>

    @Query("SELECT b FROM Book b WHERE b.itemPage < 200")
    fun findAllUnder200Pages(): List<Book>

    @Query("SELECT b FROM Book b WHERE b.isBestseller = true")
    fun findAllBestsellers(): List<Book>
}
