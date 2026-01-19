# Commerce Smart - Full Stack E-Commerce Platform

A modern, full-stack e-commerce application built with Spring Boot backend and React frontend.

## 🚀 Quick Start

### Backend Setup

1. **Prerequisites**
   - Java 25
   - PostgreSQL database
   - Maven

2. **Database Configuration**
   Create a `.env` file in the project root:
   ```env
   DATABASE_URL=jdbc:postgresql://localhost:5432/commerce
   DATABASE_USERNAME=your_username
   DATABASE_PASSWORD=your_password
   ```

3. **Run Backend**
   ```bash
   ./mvnw spring-boot:run
   ```
   Backend will start on `http://localhost:8080`

### Frontend Setup

1. **Prerequisites**
   - Node.js 14+
   - npm

2. **Install & Run**
   ```bash
   cd frontend
   npm install
   npm start
   ```
   Frontend will start on `http://localhost:3000`

## 📋 Features

### 🛍️ Customer Features
- ✅ User registration and authentication
- ✅ Browse products with search and category filters
- ✅ Product details with real-time inventory
- ✅ Shopping cart management
- ✅ Checkout and order placement
- ✅ Order history and tracking
- ✅ User profile management

### 👨‍💼 Admin Features
- ✅ Product management (CRUD)
- ✅ Category management (CRUD)
- ✅ Inventory management
- ✅ Order status management
- ✅ User management
- ✅ Admin dashboard with overview

### 🔒 Security
- ✅ JWT-based authentication
- ✅ Role-based access control (CUSTOMER, SELLER, ADMIN)
- ✅ Password encryption with BCrypt
- ✅ Custom authentication interceptors
- ✅ CORS configuration

## 🏗️ Architecture

### Backend (Spring Boot)
```
src/main/java/com/example/Commerce/
├── Aspects/           # AOP logging
├── Config/            # Security & Web configuration
├── Controllers/       # REST API endpoints
├── DTOs/              # Data Transfer Objects
├── Entities/          # JPA entities
├── Enums/             # Status enums
├── errorHandlers/     # Exception handling
├── Mappers/           # MapStruct mappers
├── Repositories/      # JPA repositories
└── Services/          # Business logic
```

### Frontend (React)
```
frontend/src/
├── components/        # Reusable components
├── context/           # React Context (Auth, Cart)
├── pages/             # Page components
├── services/          # API services
└── App.js             # Main application
```

## 🔌 API Endpoints

### Authentication
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - User login
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/updateProfile` - Update profile

### Products
- `GET /api/products/public/all` - Get all products (public)
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products/add` - Add product (Admin)
- `PUT /api/products/update/{id}` - Update product (Admin)
- `DELETE /api/products/{id}` - Delete product (Admin)

### Categories
- `GET /api/categories/public/all` - Get all categories (public)
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories/add` - Add category (Admin)
- `PUT /api/categories/update/{id}` - Update category (Admin)
- `DELETE /api/categories/{id}` - Delete category (Admin)

### Orders
- `POST /api/orders/create` - Create order (Customer)
- `GET /api/orders/user` - Get user orders (Customer)
- `GET /api/orders/all` - Get all orders (Admin)
- `GET /api/orders/{id}` - Get order by ID
- `PUT /api/orders/update/{id}` - Update order status (Admin)
- `DELETE /api/orders/{id}` - Delete order (Admin)

### Inventory
- `GET /api/inventory/all` - Get all inventory (Admin)
- `GET /api/inventory/{id}` - Get inventory by ID
- `GET /api/inventory/product/{productId}` - Get inventory by product
- `POST /api/inventory/add` - Add inventory (Admin)
- `PUT /api/inventory/update/{id}` - Update inventory (Admin)
- `PATCH /api/inventory/adjust/{id}` - Adjust quantity (Admin)
- `DELETE /api/inventory/{id}` - Delete inventory (Admin)

## 🗄️ Database Schema

### Main Entities
- **users** - User accounts with roles
- **categories** - Product categories
- **products** - Product catalog
- **inventory** - Stock management
- **orders** - Customer orders
- **order_items** - Order line items

### Relationships
- User → Orders (One-to-Many)
- Category → Products (One-to-Many)
- Product → Inventory (One-to-One)
- Order → OrderItems (One-to-Many)
- Product → OrderItems (One-to-Many)

## 🔐 Authentication Flow

1. User registers/logs in
2. Backend returns JWT token
3. Frontend stores token in localStorage
4. Token sent with each API request in Authorization header
5. Backend validates token and extracts user info
6. AuthInterceptor sets user context in request

## 🎨 UI/UX Features

- **Modern Design** - Clean, sleek interface with Tailwind CSS
- **Responsive** - Works on desktop, tablet, and mobile
- **Smooth Animations** - Transitions and hover effects
- **Loading States** - Skeleton loaders and spinners
- **Error Handling** - User-friendly error messages
- **Toast Notifications** - Success/error feedback

## 📦 Tech Stack

### Backend
- **Spring Boot 4.0.1** - Framework
- **Spring Data JPA** - ORM
- **PostgreSQL** - Database
- **MapStruct** - Object mapping
- **BCrypt** - Password hashing
- **Lombok** - Boilerplate reduction
- **Swagger/OpenAPI** - API documentation

### Frontend
- **React 18** - UI library
- **React Router v6** - Routing
- **Tailwind CSS** - Styling
- **Axios** - HTTP client
- **Heroicons** - Icons
- **date-fns** - Date formatting

## 🧪 Testing

### Backend
```bash
./mvnw test
```

### Frontend
```bash
cd frontend
npm test
```

## 📝 Default Users

After seeding, the following users are available:

**Admin:**
- Email: `admin@commerce.com`
- Password: `admin123`

**Seller:**
- Email: `seller@commerce.com`
- Password: `seller123`

**Customer:**
- Email: `jane.doe@example.com`
- Password: `customer123`

## 🚀 Deployment

### Backend
```bash
./mvnw clean package
java -jar target/Commerce-0.0.1-SNAPSHOT.jar
```

### Frontend
```bash
cd frontend
npm run build
# Serve the build folder with your preferred web server
```

## 📚 Documentation

- Backend API: `http://localhost:8080/swagger-ui.html`
- Frontend README: [frontend/README.md](frontend/README.md)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- Abdul Basit

## 🙏 Acknowledgments

- Spring Boot team for the amazing framework
- React team for the powerful UI library
- All open-source contributors
