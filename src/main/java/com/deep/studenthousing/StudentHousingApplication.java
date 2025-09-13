package com.deep.studenthousing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class StudentHousingApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudentHousingApplication.class, args);
	}

}
