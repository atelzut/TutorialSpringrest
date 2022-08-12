package com.example.demo.controllers;

import com.example.demo.assembler.EmployeeAssembler;
import com.example.demo.entities.Employee;
import com.example.demo.exceptions.EmployeeNotFoundException;
import com.example.demo.repositories.EmployeeRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


@RestController
public class EmployeeController {

    private EmployeeRepository repository;
    private EmployeeAssembler assembler;

    public EmployeeController(EmployeeRepository repository, EmployeeAssembler employeeAssembler) {
        this.repository = repository;
        this.assembler=employeeAssembler;
    }

    @GetMapping("/employees")
    public CollectionModel<EntityModel<Employee>> all() {

        List<EntityModel<Employee>> employees = repository.findAll().stream()
                .map(employee -> {
                    try {
                        return EntityModel.of(employee,
                                linkTo(methodOn(EmployeeController.class).one(employee.getId())).withSelfRel(),
                                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
                    } catch (EmployeeNotFoundException e) {
                        e.printStackTrace();
                        return EntityModel.of(employee, linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
                    }
                })
                .toList();

        return CollectionModel.of(employees, linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
    }


    @PostMapping("/employees")
    ResponseEntity newEmployee(@RequestBody Employee employee) {
        EntityModel<Employee> model = assembler.toModel(repository.save(employee));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @GetMapping("/employees/{id}")
    public EntityModel<Employee> one(@PathVariable Long id) throws EmployeeNotFoundException {

        Employee employee = repository.findById(id) //
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        return assembler.toModel(employee);
    }

    @PutMapping("/employees/{id}")
    ResponseEntity replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {
        Employee entity = repository.findById(id).map(employee -> {
            employee.setName(newEmployee.getName());
            employee.setRole(newEmployee.getRole());
            return repository.save(employee);
        }).orElseGet(() -> {
            newEmployee.setId(id);
            return repository.save(newEmployee);
        });
        EntityModel<Employee> model = assembler.toModel(entity);
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @DeleteMapping ("/employees/{id}")
    ResponseEntity deletEmployee(@PathVariable Long id) throws EmployeeNotFoundException {
        repository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

}
