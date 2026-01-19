# E-Commerce Backend API

This is a backend project for an E-commerce application built using **Spring Boot**, **MongoDB**, and **Razorpay**. It allows users to browse products, add them to a cart, place orders, and make payments.

## 🚀 Features

- **User & Product Management**: Browse and search products.
- **Cart System**: Add, view, and clear items in the cart.
- **Order Processing**: Place orders with real-time stock management.
- **Payment Integration**: Secure payments using **Razorpay** (Test Mode).
- **Webhooks**: Automatic order status updates upon successful payment.
- **Bonus Features**: Order history, order cancellation, and product search.

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3** (Web, Data MongoDB)
- **MongoDB Atlas** (Cloud Database)
- **Razorpay** (Payment Gateway)
- **Maven** (Build Tool)

## ⚙️ Setup & Installation

1.  **Clone/Download** the project.
2.  **Install Java 17** and **Maven**.
3.  Update `src/main/resources/application.yaml` with your credentials:
    ```yaml
    spring:
      data:
        mongodb:
          uri: <YOUR_MONGODB_CONNECTION_STRING>
    razorpay:
      key-id: <YOUR_RAZORPAY_KEY_ID>
      key-secret: <YOUR_RAZORPAY_KEY_SECRET>
    ```
4.  **Run the application**:
    ```bash
    mvn spring-boot:run
    ```
    The server will start at `http://localhost:8080`.

## 📡 API Endpoints

### 🛍️ Products
- `POST /api/products` - Add a new product.
- `GET /api/products` - List all products.
- `GET /api/products/search?q=query` - Search for products (e.g., ?q=laptop).

### 🛒 Cart
- `POST /api/cart/add` - Add item to cart.
- `GET /api/cart/{userId}` - View user's cart.
- `DELETE /api/cart/{userId}/clear` - Clear user's cart.

### 📦 Orders
- `POST /api/orders` - Place an order from the cart.
- `GET /api/orders/{orderId}` - View order details.
- `GET /api/orders/user/{userId}` - View order history.
- `POST /api/orders/{orderId}/cancel` - Cancel an order.

### 💳 Payments
- `POST /api/payments/create` - Initiate a Razorpay payment.
- `POST /api/webhooks/payment` - Webhook for payment success.

## 🧪 Testing

Import the included Postman collection (`postman/E-Commerce-API.postman_collection.json`) to test all endpoints easily.
