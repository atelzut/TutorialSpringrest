package com.example.demo.repositories;

import com.example.demo.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * come tipo abbiamo messo Employee perché naturalmente è l'entiry dove andiamo a lavorare e Long perchè corrisponde all'Id
 */

public interface EmployeeRepository extends JpaRepository<Employee,Long> {


}
