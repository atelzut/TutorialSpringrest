package com.example.demo.controllers;

import com.example.demo.assembler.OrderAssembler;
import com.example.demo.entities.Order;
import com.example.demo.entities.Status;
import com.example.demo.exceptions.OrderNotFoundException;
import com.example.demo.repositories.OrderRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class OrderControllerV2 {

    private OrderRepository orderRepository;
    private final OrderAssembler orderAssembler;

    public OrderControllerV2(OrderRepository orderRepository, OrderAssembler orderAssembler) {
        this.orderRepository = orderRepository;
        this.orderAssembler = orderAssembler;
    }

    @GetMapping("/orders/v2")
    public CollectionModel<EntityModel<Order>> all() {
        List<EntityModel<Order>> orderList = orderRepository.findAll().stream()
                .map(orderAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(orderList, linkTo(methodOn(OrderControllerV2.class).all()).withSelfRel());
    }

    @GetMapping("/orders/v2/{id}")
    public EntityModel<Order> one(@PathVariable Long id) throws OrderNotFoundException {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        EntityModel model = EntityModel.of(order);

        return orderAssembler.toModel(order);
    }

    @PostMapping("/orders/v2")
    public ResponseEntity newOrder(@RequestBody Order newOrder) throws OrderNotFoundException {
        newOrder.setStatus(Status.IN_PROGRESS);
        Order order = orderRepository.save(newOrder);
        EntityModel<Order> orderModel = EntityModel.of(order);
        // return ResponseEntity.created(orderModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(order);
        return ResponseEntity
                .created(linkTo(methodOn(OrderControllerV2.class).one(order.getId())).toUri()) //
                .body(orderAssembler.toModel(order));
    }

    @DeleteMapping("/orders/v2/{id}/cancel")
    public ResponseEntity cancel(@PathVariable Long id) throws OrderNotFoundException {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus().equals(Status.IN_PROGRESS)) {
            order.setStatus(Status.CANCELLED);
            return ResponseEntity.ok(orderAssembler.toModel(orderRepository.save(order)));
        }

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(
                        Problem.create()
                                .withTitle("Method not allowed")
                                .withDetail("You cann not cancel an order that is in " + order.getStatus() + " status")
                );
    }

    @PutMapping("/orders/v2/{id}/complete")
    public ResponseEntity complete(@PathVariable Long id) throws OrderNotFoundException {

        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus().equals(Status.IN_PROGRESS)) {
            order.setStatus(Status.COMPLETED);
            return ResponseEntity.ok(orderAssembler.toModel(orderRepository.save(order)));
        }

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(
                        Problem.create()
                                .withTitle("Method not allowed")
                                .withDetail("You cann not complete an order that is in " + order.getStatus() + " status")
                );
    }
}
