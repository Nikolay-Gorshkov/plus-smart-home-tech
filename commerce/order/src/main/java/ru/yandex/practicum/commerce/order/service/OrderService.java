package ru.yandex.practicum.commerce.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.api.client.DeliveryClient;
import ru.yandex.practicum.commerce.interaction.api.client.PaymentClient;
import ru.yandex.practicum.commerce.interaction.api.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderState;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.interaction.api.exception.BadRequestException;
import ru.yandex.practicum.commerce.interaction.api.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.interaction.api.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.interaction.api.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.interaction.api.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.commerce.interaction.api.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.request.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.request.PlanDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.api.request.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.entity.OrderEntity;
import ru.yandex.practicum.commerce.order.mapper.OrderMapper;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final WarehouseClient warehouseClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;

    public OrderService(OrderRepository orderRepository,
                        OrderMapper orderMapper,
                        WarehouseClient warehouseClient,
                        PaymentClient paymentClient,
                        DeliveryClient deliveryClient) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.warehouseClient = warehouseClient;
        this.paymentClient = paymentClient;
        this.deliveryClient = deliveryClient;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException();
        }
        return orderMapper.toDtoList(orderRepository.findAllByUsernameOrderByCreatedAtDesc(username));
    }

    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        if (request.shoppingCart().products().isEmpty()) {
            throw new BadRequestException("Shopping cart is empty");
        }
        BookedProductsDto bookedProductsDto = checkShoppingCart(request);
        OrderEntity entity = orderMapper.toEntity(request, bookedProductsDto);
        return orderMapper.toDto(orderRepository.save(entity));
    }

    public OrderDto productReturn(ProductReturnRequest request) {
        OrderEntity order = getOrderOrThrow(request.orderId());
        acceptReturn(request.products());
        order.setState(OrderState.PRODUCT_RETURNED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto payment(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        if (order.getPaymentId() == null) {
            ensureTotalPrice(order);
            PaymentDto paymentDto = createPayment(order);
            order.setPaymentId(paymentDto.paymentId());
            order.setState(OrderState.ON_PAYMENT);
            return orderMapper.toDto(orderRepository.save(order));
        }
        if (order.getState() != OrderState.PAID) {
            order.setState(OrderState.PAID);
        }
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto paymentFailed(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto delivery(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setState(OrderState.DELIVERED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto deliveryFailed(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto complete(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setState(OrderState.COMPLETED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto calculateTotalCost(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        ensureTotalPrice(order);
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto calculateDeliveryCost(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        ensureDeliveryPrice(order);
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto assembly(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        if (order.getState() == OrderState.ASSEMBLED) {
            return orderMapper.toDto(order);
        }
        if (order.getState() == OrderState.DELIVERED
                || order.getState() == OrderState.COMPLETED) {
            return orderMapper.toDto(order);
        }
        BookedProductsDto bookedProductsDto = reserveProducts(order);
        order.setDeliveryWeight(bookedProductsDto.deliveryWeight());
        order.setDeliveryVolume(bookedProductsDto.deliveryVolume());
        order.setFragile(bookedProductsDto.fragile());
        order.setState(OrderState.ASSEMBLED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto assemblyFailed(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    private BookedProductsDto checkShoppingCart(CreateNewOrderRequest request) {
        try {
            return warehouseClient.checkProductQuantityEnoughForShoppingCart(request.shoppingCart());
        } catch (FeignException exception) {
            if (exception.status() == 400) {
                throw new NoSpecifiedProductInWarehouseException("Нет заказываемого товара на складе");
            }
            throw new BadRequestException("Warehouse service is unavailable");
        }
    }

    private void acceptReturn(Map<UUID, Long> products) {
        try {
            warehouseClient.acceptReturn(products);
        } catch (FeignException exception) {
            throw new BadRequestException("Warehouse service is unavailable");
        }
    }

    private PaymentDto createPayment(OrderEntity order) {
        try {
            return paymentClient.payment(orderMapper.toDto(order));
        } catch (FeignException exception) {
            if (exception.status() == 400) {
                throw new NotEnoughInfoInOrderToCalculateException("Недостаточно данных для формирования оплаты");
            }
            throw new BadRequestException("Payment service is unavailable");
        }
    }

    private BookedProductsDto reserveProducts(OrderEntity order) {
        try {
            return warehouseClient.assemblyProductsForOrder(
                    new AssemblyProductsForOrderRequest(order.getProducts(), order.getOrderId())
            );
        } catch (FeignException exception) {
            if (exception.status() == 400) {
                throw new NoSpecifiedProductInWarehouseException("Нет заказываемого товара на складе");
            }
            throw new BadRequestException("Warehouse service is unavailable");
        }
    }

    private void ensureProductPrice(OrderEntity order) {
        if (order.getProductPrice() != null) {
            return;
        }
        try {
            BigDecimal productPrice = paymentClient.productCost(orderMapper.toDto(order));
            order.setProductPrice(productPrice);
        } catch (FeignException exception) {
            if (exception.status() == 400) {
                throw new NotEnoughInfoInOrderToCalculateException("Недостаточно информации для расчёта стоимости товаров");
            }
            throw new BadRequestException("Payment service is unavailable");
        }
    }

    private void ensureDeliveryPrice(OrderEntity order) {
        if (order.getDeliveryPrice() != null) {
            return;
        }
        ensureDeliveryPlanned(order);
        try {
            BigDecimal deliveryPrice = deliveryClient.deliveryCost(buildPlanDeliveryRequest(order));
            order.setDeliveryPrice(deliveryPrice);
        } catch (FeignException exception) {
            if (exception.status() == 400) {
                throw new NotEnoughInfoInOrderToCalculateException("Недостаточно информации для расчёта стоимости доставки");
            }
            throw new BadRequestException("Delivery service is unavailable");
        }
    }

    private void ensureTotalPrice(OrderEntity order) {
        ensureProductPrice(order);
        ensureDeliveryPrice(order);
        if (order.getTotalPrice() != null) {
            return;
        }
        try {
            BigDecimal totalPrice = paymentClient.getTotalCost(orderMapper.toDto(order));
            order.setTotalPrice(totalPrice);
        } catch (FeignException exception) {
            if (exception.status() == 400) {
                throw new NotEnoughInfoInOrderToCalculateException("Недостаточно информации для расчёта полной стоимости");
            }
            throw new BadRequestException("Payment service is unavailable");
        }
    }

    private void ensureDeliveryPlanned(OrderEntity order) {
        if (order.getDeliveryId() != null) {
            return;
        }
        if (order.getDeliveryWeight() == null || order.getDeliveryVolume() == null || order.getFragile() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Не хватает данных о весе, объёме или хрупкости заказа");
        }
        try {
            var deliveryDto = deliveryClient.planDelivery(buildPlanDeliveryRequest(order));
            order.setDeliveryId(deliveryDto.deliveryId());
        } catch (FeignException exception) {
            if (exception.status() == 400) {
                throw new NotEnoughInfoInOrderToCalculateException("Недостаточно информации для создания доставки");
            }
            throw new BadRequestException("Delivery service is unavailable");
        }
    }

    private PlanDeliveryRequest buildPlanDeliveryRequest(OrderEntity order) {
        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();
        return orderMapper.toPlanDeliveryRequest(order, warehouseAddress);
    }

    private OrderEntity getOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(orderId));
    }
}
