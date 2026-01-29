# GraphQL Queries and Mutations

## Order

### Queries
```graphql
query {
  allOrders {
    id
    userId
    totalAmount
    status
    items {
      id
      productId
      productName
      quantity
      totalPrice
    }
  }
}

query {
  orderById(id: 1) {
    id
    userId
    totalAmount
    status
    items {
      id
      productId
      productName
      quantity
      totalPrice
    }
  }
}

query {
  ordersByUserId(userId: 1) {
    id
    totalAmount
    status
    items {
      id
      productId
      productName
      quantity
      totalPrice
    }
  }
}
```

### Mutations
```graphql
mutation {
  createOrder(input: {
    userId: 1,
    items: [
      { productId: 2, quantity: 1 }
    ]
  }) {
    id
    status
  }
}

mutation {
  updateOrderStatus(id: 1, input: { status: SHIPPED }) {
    id
    status
  }
}
```

---

## Product

### Queries
```graphql
query {
  allProducts {
    id
    name
    price
    quantity
    categoryName
  }
}

query {
  productById(id: 1) {
    id
    name
    price
    quantity
    categoryName
  }
}
```

### Mutations
```graphql
mutation {
  addProduct(input: {
    name: "New Product",
    categoryId: 1,
    sku: "SKU123",
    price: 99.99
  }) {
    id
    name
  }
}

mutation {
  deleteProduct(id: 1)
}
```

---

## Inventory

### Queries
```graphql
query {
  allInventories {
    id
    productId
    productName
    quantity
    location
  }
}

query {
  inventoryById(id: 1) {
    id
    productId
    productName
    quantity
    location
  }
}

query {
  inventoryByProductId(productId: 1) {
    id
    productId
    productName
    quantity
    location
  }
}
```

### Mutations
```graphql
mutation {
  addInventory(input: {
    productId: 1,
    quantity: 100,
    location: "Warehouse A"
  }) {
    id
    productId
    quantity
    location
  }
}

mutation {
  updateInventory(id: 1, input: { quantity: 200, location: "Warehouse B" }) {
    id
    quantity
    location
  }
}

mutation {
  deleteInventory(id: 1)
}
```

---

## Category

### Queries
```graphql
query {
  allCategories {
    id
    name
    description
  }
}

query {
  categoryById(id: 1) {
    id
    name
    description
  }
}
```

### Mutations
```graphql
mutation {
  addCategory(input: { name: "Electronics", description: "Electronic items" }) {
    id
    name
  }
}

mutation {
  updateCategory(id: 1, input: { name: "Updated Name", description: "Updated Desc" }) {
    id
    name
    description
  }
}

mutation {
  deleteCategory(id: 1)
}
```
