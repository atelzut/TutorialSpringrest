package com.example.demo.controllers;

import com.example.demo.assembler.OrderAssembler;
import com.example.demo.entities.Order;
import com.example.demo.entities.Status;
import com.example.demo.exceptions.OrderNotFoundException;
import com.example.demo.repositories.OrderRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class OrderControllerV1 {

    private OrderRepository orderRepository;
    private OrderAssembler orderAssembler;

    public OrderControllerV1(OrderRepository orderRepository, OrderAssembler orderAssembler) {
        this.orderRepository = orderRepository;
        this.orderAssembler = orderAssembler;
    }

    @GetMapping("/orders/v1")
    public CollectionModel<EntityModel<Order>> all() {
        List<EntityModel<Order>> orderList = orderRepository.findAll().stream().
                map(order -> {
                    try {
                        return EntityModel.of(order,
                                linkTo(methodOn(OrderControllerV1.class).one(order.getId())).withSelfRel(),
                                linkTo(methodOn(OrderControllerV1.class).all()).withRel("orders"));
                    } catch (OrderNotFoundException e) {
                        e.printStackTrace();
                        return EntityModel.of(order, linkTo(methodOn(OrderControllerV1.class).all()).withRel("orders"));
                    }
                })
                .toList();

        return CollectionModel.of(orderList, linkTo(methodOn(OrderControllerV1.class).all()).withSelfRel());
    }

    @GetMapping("/orders/v1/{id}")
    public EntityModel<Order> one(@PathVariable Long id) throws OrderNotFoundException {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        EntityModel model = EntityModel.of(order);

        return orderAssembler.toModel(order);
    }

    @PostMapping("/orders/v1")
    public ResponseEntity newOrder(@RequestBody Order newOrder) throws OrderNotFoundException {
        newOrder.setStatus(Status.IN_PROGRESS);
        Order order = orderRepository.save(newOrder);
        EntityModel<Order> orderModel = EntityModel.of(order);
        // return ResponseEntity.created(orderModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(order);
        return ResponseEntity
                .created(linkTo(methodOn(OrderControllerV1.class).one(order.getId())).toUri()) //
                .body(orderAssembler.toModel(order));
    }
}
