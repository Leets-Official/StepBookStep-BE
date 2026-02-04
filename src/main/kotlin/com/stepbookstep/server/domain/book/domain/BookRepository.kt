package com.stepbookstep.server.domain.book.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BookRepository : JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    fun findAllByLevel(level: Int): List<Book>
    fun findAllByIdIn(ids: List<Long>):List<Book>

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

    @Query("SELECT b FROM Book b WHERE b.itemPage BETWEEN :minPage AND :maxPage")
    fun findAllByPageRange(@Param("minPage") minPage: Int, @Param("maxPage") maxPage: Int): List<Book>

    @Query("SELECT b FROM Book b WHERE b.score BETWEEN :minScore AND :maxScore")
    fun findAllByScoreRange(@Param("minScore") minScore: Int, @Param("maxScore") maxScore: Int): List<Book>

    @Query("SELECT b FROM Book b WHERE b.level = 3")
    fun findAllByLevel3(): List<Book>

    @Query("SELECT b FROM Book b WHERE b.categoryId = :categoryId")
    fun findAllByCategoryId(@Param("categoryId") categoryId: Long): List<Book>

    @Query("SELECT b FROM Book b WHERE b.genreId = :genreId")
    fun findAllByGenreId(@Param("genreId") genreId: Long): List<Book>
}
