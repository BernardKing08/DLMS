-- Seed catalog data. Uses explicit high IDs (1000+) and INSERT IGNORE so this
-- is safe to re-run on every startup (spring.sql.init.mode: always) without
-- colliding with manually-created books or duplicating rows on restart.
--
-- cover_image_url cycles through the 12 stock cover images already bundled
-- in the frontend (static/images/books-media/gird-view/) - there's no real
-- per-title cover art in this system, just enough visual variety that the
-- catalog grid doesn't show one identical image for every book.

INSERT IGNORE INTO books (id, title, author, isbn, category, description, cover_image_url, active) VALUES
(1001, 'The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', 'Classic Literature', 'A tragic tale of wealth, love, and the American Dream in the Jazz Age.', '/images/books-media/gird-view/book-media-grid-01.jpg', TRUE),
(1002, 'Pride and Prejudice', 'Jane Austen', '9780141439518', 'Classic Literature', 'Elizabeth Bennet navigates love and reputation in Georgian England.', '/images/books-media/gird-view/book-media-grid-02.jpg', TRUE),
(1003, 'Moby-Dick', 'Herman Melville', '9781503280786', 'Classic Literature', 'Captain Ahab''s obsessive hunt for the great white whale.', '/images/books-media/gird-view/book-media-grid-03.jpg', TRUE),
(1004, 'Jane Eyre', 'Charlotte Bronte', '9780142437209', 'Classic Literature', 'An orphan''s journey through hardship to independence and love.', '/images/books-media/gird-view/book-media-grid-04.jpg', TRUE),
(1005, 'Wuthering Heights', 'Emily Bronte', '9780141439556', 'Classic Literature', 'A tale of passion and revenge on the Yorkshire moors.', '/images/books-media/gird-view/book-media-grid-05.jpg', TRUE),
(1006, 'War and Peace', 'Leo Tolstoy', '9781400079988', 'Classic Literature', 'Epic account of Russian society during the Napoleonic wars.', '/images/books-media/gird-view/book-media-grid-06.jpg', TRUE),
(1007, 'Anna Karenina', 'Leo Tolstoy', '9780143035008', 'Classic Literature', 'A married aristocrat''s affair scandalizes Russian high society.', '/images/books-media/gird-view/book-media-grid-07.jpg', TRUE),

(1010, '1984', 'George Orwell', '9780451524935', 'Science Fiction', 'A dystopian vision of a totalitarian surveillance state.', '/images/books-media/gird-view/book-media-grid-08.jpg', TRUE),
(1011, 'Brave New World', 'Aldous Huxley', '9780060850524', 'Science Fiction', 'A genetically engineered future society built on pleasure and control.', '/images/books-media/gird-view/book-media-grid-09.jpg', TRUE),
(1012, 'Dune', 'Frank Herbert', '9780441172719', 'Science Fiction', 'Political intrigue and prophecy on the desert planet Arrakis.', '/images/books-media/gird-view/book-media-grid-10.jpg', TRUE),
(1013, 'Foundation', 'Isaac Asimov', '9780553293357', 'Science Fiction', 'A mathematician predicts the fall of a galactic empire.', '/images/books-media/gird-view/book-media-grid-11.jpg', TRUE),
(1014, 'Neuromancer', 'William Gibson', '9780441569595', 'Science Fiction', 'A washed-up hacker is hired for one last, impossible job.', '/images/books-media/gird-view/book-media-grid-12.jpg', TRUE),
(1015, 'The Martian', 'Andy Weir', '9780553418026', 'Science Fiction', 'An astronaut stranded on Mars fights to survive with ingenuity alone.', '/images/books-media/gird-view/book-media-grid-01.jpg', TRUE),
(1016, 'Fahrenheit 451', 'Ray Bradbury', '9781451673319', 'Science Fiction', 'A fireman whose job is burning books begins to question everything.', '/images/books-media/gird-view/book-media-grid-02.jpg', TRUE),

(1020, 'The Hobbit', 'J.R.R. Tolkien', '9780547928227', 'Fantasy', 'A reluctant hobbit joins a quest to reclaim a dwarven kingdom.', '/images/books-media/gird-view/book-media-grid-03.jpg', TRUE),
(1021, 'The Fellowship of the Ring', 'J.R.R. Tolkien', '9780547928210', 'Fantasy', 'The first volume of the epic quest to destroy the One Ring.', '/images/books-media/gird-view/book-media-grid-04.jpg', TRUE),
(1022, 'A Game of Thrones', 'George R.R. Martin', '9780553593716', 'Fantasy', 'Noble houses vie for the Iron Throne of Westeros.', '/images/books-media/gird-view/book-media-grid-05.jpg', TRUE),
(1023, 'The Name of the Wind', 'Patrick Rothfuss', '9780756404079', 'Fantasy', 'A legendary figure recounts his rise from orphan to legend.', '/images/books-media/gird-view/book-media-grid-06.jpg', TRUE),
(1024, 'Mistborn', 'Brandon Sanderson', '9780765311788', 'Fantasy', 'A street urchin discovers she can wield the magic of Allomancy.', '/images/books-media/gird-view/book-media-grid-07.jpg', TRUE),
(1025, 'The Chronicles of Narnia', 'C.S. Lewis', '9780066238500', 'Fantasy', 'Children discover a magical land through the back of a wardrobe.', '/images/books-media/gird-view/book-media-grid-08.jpg', TRUE),

(1030, 'Sherlock Holmes: A Study in Scarlet', 'Arthur Conan Doyle', '9781420951875', 'Mystery & Thriller', 'The first case that introduces the world''s greatest detective.', '/images/books-media/gird-view/book-media-grid-09.jpg', TRUE),
(1031, 'And Then There Were None', 'Agatha Christie', '9780062073488', 'Mystery & Thriller', 'Ten strangers are lured to an island and murdered one by one.', '/images/books-media/gird-view/book-media-grid-10.jpg', TRUE),
(1032, 'Gone Girl', 'Gillian Flynn', '9780307588364', 'Mystery & Thriller', 'A woman disappears on her wedding anniversary under suspicious circumstances.', '/images/books-media/gird-view/book-media-grid-11.jpg', TRUE),
(1033, 'The Girl with the Dragon Tattoo', 'Stieg Larsson', '9780307949486', 'Mystery & Thriller', 'A journalist and a hacker investigate a decades-old disappearance.', '/images/books-media/gird-view/book-media-grid-12.jpg', TRUE),
(1034, 'The Da Vinci Code', 'Dan Brown', '9780307474278', 'Mystery & Thriller', 'A symbologist uncovers a religious conspiracy hidden in art.', '/images/books-media/gird-view/book-media-grid-01.jpg', TRUE),

(1040, 'Pride, Prejudice and Romance', 'Various Authors', '9780000000001', 'Romance', 'A collection of beloved romantic classics.', '/images/books-media/gird-view/book-media-grid-02.jpg', TRUE),
(1041, 'Outlander', 'Diana Gabaldon', '9780440212560', 'Romance', 'A nurse from 1945 is swept back in time to 18th-century Scotland.', '/images/books-media/gird-view/book-media-grid-03.jpg', TRUE),
(1042, 'The Notebook', 'Nicholas Sparks', '9780446605236', 'Romance', 'A lifelong love story told through the pages of a notebook.', '/images/books-media/gird-view/book-media-grid-04.jpg', TRUE),

(1050, 'Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', '9780062316097', 'Non-Fiction', 'How Homo sapiens came to dominate the world.', '/images/books-media/gird-view/book-media-grid-05.jpg', TRUE),
(1051, 'Educated', 'Tara Westover', '9780399590504', 'Non-Fiction', 'A woman raised in a survivalist family pursues education against all odds.', '/images/books-media/gird-view/book-media-grid-06.jpg', TRUE),
(1052, 'Thinking, Fast and Slow', 'Daniel Kahneman', '9780374533557', 'Non-Fiction', 'A Nobel laureate explores the two systems that drive human thought.', '/images/books-media/gird-view/book-media-grid-07.jpg', TRUE),
(1053, 'The Immortal Life of Henrietta Lacks', 'Rebecca Skloot', '9781400052189', 'Non-Fiction', 'The story behind the cells that revolutionized medicine.', '/images/books-media/gird-view/book-media-grid-08.jpg', TRUE),
(1054, 'Atomic Habits', 'James Clear', '9780735211292', 'Self-Help', 'A practical guide to building good habits and breaking bad ones.', '/images/books-media/gird-view/book-media-grid-09.jpg', TRUE),
(1055, 'The 7 Habits of Highly Effective People', 'Stephen R. Covey', '9781982137274', 'Self-Help', 'A principle-centered approach to personal and professional effectiveness.', '/images/books-media/gird-view/book-media-grid-10.jpg', TRUE),

(1060, 'Steve Jobs', 'Walter Isaacson', '9781451648539', 'Biography', 'The definitive biography of Apple''s co-founder.', '/images/books-media/gird-view/book-media-grid-11.jpg', TRUE),
(1061, 'The Diary of a Young Girl', 'Anne Frank', '9780553296983', 'Biography', 'A teenager''s account of hiding from the Nazis in occupied Amsterdam.', '/images/books-media/gird-view/book-media-grid-12.jpg', TRUE),
(1062, 'Long Walk to Freedom', 'Nelson Mandela', '9780316548182', 'Biography', 'Nelson Mandela''s own account of his fight against apartheid.', '/images/books-media/gird-view/book-media-grid-01.jpg', TRUE),

(1070, 'Guns, Germs, and Steel', 'Jared Diamond', '9780393354324', 'History', 'Why some civilizations conquered others across human history.', '/images/books-media/gird-view/book-media-grid-02.jpg', TRUE),
(1071, 'A People''s History of the United States', 'Howard Zinn', '9780062397348', 'History', 'American history told from the perspective of ordinary people.', '/images/books-media/gird-view/book-media-grid-03.jpg', TRUE),
(1072, 'The Guns of August', 'Barbara W. Tuchman', '9780345476098', 'History', 'The dramatic opening month of the First World War.', '/images/books-media/gird-view/book-media-grid-04.jpg', TRUE),

(1080, 'A Brief History of Time', 'Stephen Hawking', '9780553380163', 'Science', 'An accessible exploration of cosmology and the nature of time.', '/images/books-media/gird-view/book-media-grid-05.jpg', TRUE),
(1081, 'Cosmos', 'Carl Sagan', '9780345539434', 'Science', 'A sweeping journey through the universe and the history of science.', '/images/books-media/gird-view/book-media-grid-06.jpg', TRUE),
(1082, 'The Selfish Gene', 'Richard Dawkins', '9780198788607', 'Science', 'A gene-centered view of evolution that reshaped biology.', '/images/books-media/gird-view/book-media-grid-07.jpg', TRUE),
(1083, 'Silent Spring', 'Rachel Carson', '9780618249060', 'Science', 'The book that launched the modern environmental movement.', '/images/books-media/gird-view/book-media-grid-08.jpg', TRUE),

(1090, 'Charlotte''s Web', 'E.B. White', '9780061124952', 'Children''s', 'A pig named Wilbur is saved by his friendship with a clever spider.', '/images/books-media/gird-view/book-media-grid-09.jpg', TRUE),
(1091, 'Where the Wild Things Are', 'Maurice Sendak', '9780064431781', 'Children''s', 'A boy sails to an island of monsters who make him their king.', '/images/books-media/gird-view/book-media-grid-10.jpg', TRUE),
(1092, 'Matilda', 'Roald Dahl', '9780142410370', 'Children''s', 'A brilliant young girl with telekinetic powers outwits her cruel headmistress.', '/images/books-media/gird-view/book-media-grid-11.jpg', TRUE),

(1100, 'The Hunger Games', 'Suzanne Collins', '9780439023528', 'Young Adult', 'A girl volunteers for a televised fight to the death to save her sister.', '/images/books-media/gird-view/book-media-grid-12.jpg', TRUE),
(1101, 'The Fault in Our Stars', 'John Green', '9780525478812', 'Young Adult', 'Two teenagers with cancer fall in love at a support group.', '/images/books-media/gird-view/book-media-grid-01.jpg', TRUE),
(1102, 'Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', '9780590353427', 'Young Adult', 'An orphan discovers he is a wizard destined for greatness.', '/images/books-media/gird-view/book-media-grid-02.jpg', TRUE),

(1110, 'Leaves of Grass', 'Walt Whitman', '9781580495929', 'Poetry', 'A celebrated collection celebrating democracy, nature, and the self.', '/images/books-media/gird-view/book-media-grid-03.jpg', TRUE),
(1111, 'The Waste Land and Other Poems', 'T.S. Eliot', '9780156948757', 'Poetry', 'A landmark modernist poem on disillusionment after the First World War.', '/images/books-media/gird-view/book-media-grid-04.jpg', TRUE),

(1120, 'Dracula', 'Bram Stoker', '9780486411095', 'Horror', 'The vampire count Dracula terrorizes Victorian England.', '/images/books-media/gird-view/book-media-grid-05.jpg', TRUE),
(1121, 'Frankenstein', 'Mary Shelley', '9780486282114', 'Horror', 'A scientist''s creation turns monstrous in this gothic classic.', '/images/books-media/gird-view/book-media-grid-06.jpg', TRUE),
(1122, 'The Shining', 'Stephen King', '9780307743657', 'Horror', 'A family''s winter at an isolated hotel descends into supernatural terror.', '/images/books-media/gird-view/book-media-grid-07.jpg', TRUE);

-- Backfill cover_image_url for these same books if they already existed
-- (from a run before this column existed) - INSERT IGNORE above only
-- applies to genuinely new rows, so pre-existing ones need this instead.
UPDATE books SET cover_image_url = '/images/books-media/gird-view/book-media-grid-01.jpg' WHERE id IN (1001, 1015, 1034, 1062, 1101) AND cover_image_url IS NULL;
UPDATE books SET cover_image_url = '/images/books-media/gird-view/book-media-grid-02.jpg' WHERE id IN (1002, 1016, 1040, 1070, 1102) AND cover_image_url IS NULL;
UPDATE books SET cover_image_url = '/images/books-media/gird-view/book-media-grid-03.jpg' WHERE id IN (1003, 1020, 1041, 1071, 1110) AND cover_image_url IS NULL;
UPDATE books SET cover_image_url = '/images/books-media/gird-view/book-media-grid-04.jpg' WHERE id IN (1004, 1021, 1042, 1072, 1111) AND cover_image_url IS NULL;
UPDATE books SET cover_image_url = '/images/books-media/gird-view/book-media-grid-05.jpg' WHERE id IN (1005, 1022, 1050, 1080, 1120) AND cover_image_url IS NULL;
UPDATE books SET cover_image_url = '/images/books-media/gird-view/book-media-grid-06.jpg' WHERE id IN (1006, 1023, 1051, 1081, 1121) AND cover_image_url IS NULL;
UPDATE books SET cover_image_url = '/images/books-media/gird-view/book-media-grid-07.jpg' WHERE id IN (1007, 1024, 1052, 1082, 1122) AND cover_image_url IS NULL;
UPDATE books SET cover_image_url = '/images/books-media/gird-view/book-media-grid-08.jpg' WHERE id IN (1010, 1025, 1053, 1083) AND cover_image_url IS NULL;
UPDATE books SET cover_image_url = '/images/books-media/gird-view/book-media-grid-09.jpg' WHERE id IN (1011, 1030, 1054, 1090) AND cover_image_url IS NULL;
UPDATE books SET cover_image_url = '/images/books-media/gird-view/book-media-grid-10.jpg' WHERE id IN (1012, 1031, 1055, 1091) AND cover_image_url IS NULL;
UPDATE books SET cover_image_url = '/images/books-media/gird-view/book-media-grid-11.jpg' WHERE id IN (1013, 1032, 1060, 1092) AND cover_image_url IS NULL;
UPDATE books SET cover_image_url = '/images/books-media/gird-view/book-media-grid-12.jpg' WHERE id IN (1014, 1033, 1061, 1100) AND cover_image_url IS NULL;
