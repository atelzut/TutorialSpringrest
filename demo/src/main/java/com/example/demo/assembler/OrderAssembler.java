package com.example.demo.assembler;

import com.example.demo.controllers.OrderControllerV2;
import com.example.demo.entities.Order;
import com.example.demo.entities.Status;
import com.example.demo.exceptions.OrderNotFoundException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OrderAssembler implements RepresentationModelAssembler<Order, EntityModel<Order>> {

    @Override
    public EntityModel<Order> toModel(Order order) {

        EntityModel<Order> orderModel = null;
        try {
            orderModel = EntityModel.of(order,
                    linkTo(methodOn(OrderControllerV2.class).one(order.getId())).withSelfRel(),
                    linkTo(methodOn(OrderControllerV2.class).all()).withRel("orders"));
        } catch (OrderNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (order.getStatus().equals(Status.IN_PROGRESS)) {
            try {
                orderModel.add(linkTo(methodOn(OrderControllerV2.class).cancel(order.getId())).withRel("cancel"));
                orderModel.add(linkTo(methodOn(OrderControllerV2.class).complete(order.getId())).withRel("complete"));
            } catch (OrderNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return orderModel;
    }

    @Override
    public CollectionModel<EntityModel<Order>> toCollectionModel(Iterable<? extends Order> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }

}
