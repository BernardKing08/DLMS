-- Seed inventory data matching catalog/catalogs/src/main/resources/data.sql
-- book_id values (1000+ range, same reasoning: avoids colliding with
-- manually-created books, and INSERT IGNORE makes this safe to re-run).
-- Copy counts are deliberately varied - some books fully available, some
-- partially or fully checked out - so the catalog doesn't look artificial.

INSERT IGNORE INTO inventory (book_id, total_copies, available_copies, active) VALUES
(1001, 6, 2, TRUE), (1002, 5, 5, TRUE), (1003, 4, 1, TRUE), (1004, 5, 3, TRUE),
(1005, 4, 4, TRUE), (1006, 3, 0, TRUE), (1007, 4, 2, TRUE),

(1010, 8, 3, TRUE), (1011, 6, 4, TRUE), (1012, 7, 1, TRUE), (1013, 5, 5, TRUE),
(1014, 4, 2, TRUE), (1015, 6, 6, TRUE), (1016, 5, 0, TRUE),

(1020, 9, 4, TRUE), (1021, 7, 2, TRUE), (1022, 8, 3, TRUE), (1023, 5, 5, TRUE),
(1024, 6, 1, TRUE), (1025, 6, 6, TRUE),

(1030, 4, 2, TRUE), (1031, 5, 5, TRUE), (1032, 6, 0, TRUE), (1033, 4, 3, TRUE),
(1034, 7, 4, TRUE),

(1040, 3, 3, TRUE), (1041, 5, 2, TRUE), (1042, 4, 4, TRUE),

(1050, 6, 3, TRUE), (1051, 5, 5, TRUE), (1052, 6, 1, TRUE), (1053, 4, 4, TRUE),
(1054, 8, 2, TRUE), (1055, 6, 6, TRUE),

(1060, 5, 3, TRUE), (1061, 6, 6, TRUE), (1062, 4, 1, TRUE),

(1070, 4, 4, TRUE), (1071, 3, 0, TRUE), (1072, 3, 3, TRUE),

(1080, 5, 2, TRUE), (1081, 6, 6, TRUE), (1082, 4, 3, TRUE), (1083, 4, 4, TRUE),

(1090, 6, 6, TRUE), (1091, 5, 3, TRUE), (1092, 6, 2, TRUE),

(1100, 10, 4, TRUE), (1101, 6, 6, TRUE), (1102, 10, 1, TRUE),

(1110, 3, 3, TRUE), (1111, 3, 2, TRUE),

(1120, 5, 5, TRUE), (1121, 4, 2, TRUE), (1122, 6, 0, TRUE);
