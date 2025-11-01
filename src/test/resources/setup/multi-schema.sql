-- Create schemas
CREATE SCHEMA shared;
CREATE SCHEMA people;
CREATE SCHEMA school;

-- ========================
-- Shared schema
-- ========================
CREATE TABLE shared.addresses (
                                  id BIGSERIAL PRIMARY KEY,
                                  street TEXT NOT NULL,
                                  city TEXT NOT NULL,
                                  postal_code TEXT NOT NULL,
                                  country TEXT NOT NULL
);

CREATE TABLE shared.courses (
                                id BIGSERIAL PRIMARY KEY,
                                code TEXT NOT NULL UNIQUE,
                                title TEXT NOT NULL,
                                credits SMALLINT NOT NULL
);

-- ========================
-- People schema
-- ========================
CREATE TABLE people.teachers (
                                 id BIGSERIAL PRIMARY KEY,
                                 full_name TEXT NOT NULL,
                                 hire_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                 address_id BIGINT REFERENCES shared.addresses(id) ON DELETE SET NULL
);

CREATE TABLE people.students (
                                 id BIGSERIAL PRIMARY KEY,
                                 full_name TEXT NOT NULL,
                                 enrollment_year SMALLINT NOT NULL,
                                 address_id BIGINT REFERENCES shared.addresses(id) ON DELETE SET NULL
);

-- ========================
-- School schema
-- ========================
CREATE TABLE school.classrooms (
                                   id BIGSERIAL PRIMARY KEY,
                                   name TEXT NOT NULL UNIQUE,
                                   capacity SMALLINT NOT NULL
);

CREATE TABLE school.subject_assignments (
                                            id BIGSERIAL PRIMARY KEY,
                                            teacher_id BIGINT NOT NULL REFERENCES people.teachers(id) ON DELETE CASCADE,
                                            course_id BIGINT NOT NULL REFERENCES shared.courses(id) ON DELETE CASCADE,
                                            classroom_id BIGINT REFERENCES school.classrooms(id),
                                            semester TEXT NOT NULL,
                                            UNIQUE (teacher_id, course_id, semester)
);

CREATE TABLE school.enrollments (
                                    student_id BIGINT NOT NULL REFERENCES people.students(id) ON DELETE CASCADE,
                                    course_id BIGINT NOT NULL REFERENCES shared.courses(id) ON DELETE CASCADE,
                                    enrolled_on DATE NOT NULL DEFAULT CURRENT_DATE,
                                    grade CHAR(2),
                                    PRIMARY KEY (student_id, course_id)
);
