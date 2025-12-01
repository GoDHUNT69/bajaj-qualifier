package com.example.bfhl.sql;

public final class SqlQueries {

    private SqlQueries() {
    }

    public static final String QUESTION_1_QUERY = """
            -- Question 1 SQL goes here if you ever need it
            """;

    // Question 2: Employee / department average age + up to 10 names
    public static final String QUESTION_2_QUERY = """
            WITH salary_per_employee AS (
                SELECT
                    e.emp_id,
                    e.first_name,
                    e.last_name,
                    e.department AS department_id,
                    DATE_PART('year', AGE(CURRENT_DATE, e.dob))::int AS age_years,
                    SUM(p.amount) AS total_salary
                FROM employee e
                JOIN payments p
                    ON p.emp_id = e.emp_id
                GROUP BY
                    e.emp_id, e.first_name, e.last_name, e.department, e.dob
            ),
            qualified AS (
                SELECT
                    d.department_id,
                    d.department_name,
                    s.emp_id,
                    s.first_name,
                    s.last_name,
                    s.age_years,
                    s.total_salary,
                    ROW_NUMBER() OVER (
                        PARTITION BY d.department_id
                        ORDER BY s.total_salary DESC, s.emp_id
                    ) AS rn
                FROM department d
                JOIN salary_per_employee s
                    ON d.department_id = s.department_id
                WHERE s.total_salary > 70000
            ),
            agg AS (
                SELECT
                    department_id,
                    department_name,
                    AVG(age_years)::numeric(10,2) AS average_age,
                    STRING_AGG(first_name || ' ' || last_name, ', ' ORDER BY total_salary DESC, emp_id)
                        AS employee_list
                FROM qualified
                WHERE rn <= 10
                GROUP BY department_id, department_name
            )
            SELECT
                department_name,
                average_age AS average_age,
                employee_list
            FROM agg
            ORDER BY department_id DESC;
            """;
}
