package com.catalog.ctlog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("qa")
@TestPropertySource(properties = {
		"spring.cloud.config.enabled=false",
		"spring.jpa.defer-datasource-initialization=true",
		"eureka.client.enabled=false"
})
class CatalogsApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void seededBookCoversAreResolvedFromTheirIsbn() {
		String coverUrl = jdbcTemplate.queryForObject(
				"SELECT cover_image_url FROM books WHERE isbn = ?",
				String.class,
				"9780743273565"
		);

		Assertions.assertEquals(
				"https://covers.openlibrary.org/b/isbn/9780743273565-M.jpg?default=false",
				coverUrl
		);
	}

	@Test
	void everySeededBookUsesAnIsbnWithAnOpenLibraryCover() {
		Integer seededBooksWithoutCoverUrls = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM books WHERE id BETWEEN 1000 AND 1122 "
						+ "AND (cover_image_url IS NULL OR cover_image_url NOT LIKE "
						+ "'https://covers.openlibrary.org/b/isbn/%')",
				Integer.class
		);
		Integer oldMissingCoverIsbns = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM books WHERE isbn IN (?, ?, ?, ?, ?)",
				Integer.class,
				"9781503280786",
				"9781420951875",
				"9780307949486",
				"9780000000001",
				"9780156948757"
		);

		Assertions.assertEquals(0, seededBooksWithoutCoverUrls);
		Assertions.assertEquals(0, oldMissingCoverIsbns);
	}
}
