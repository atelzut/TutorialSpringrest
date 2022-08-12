package com.example.demo.exceptions;


public class EmployeeNotFoundException extends Exception {

    public EmployeeNotFoundException(Long id) {
        super("Employee not found. id= " + id);
    }


}
