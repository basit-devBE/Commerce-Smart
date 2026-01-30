package com.example.Commerce.Controllers;


import com.example.Commerce.Config.RequiresRole;
import com.example.Commerce.DTOs.AddProductDTO;
import com.example.Commerce.DTOs.ApiResponse;
import com.example.Commerce.DTOs.ErrorResponse;
import com.example.Commerce.DTOs.PagedResponse;
import com.example.Commerce.DTOs.ProductResponseDTO;
import com.example.Commerce.DTOs.UpdateProductDTO;
import com.example.Commerce.DTOs.ValidationErrorResponse;
import com.example.Commerce.Enums.UserRole;
import com.example.Commerce.interfaces.IProductService;
import com.example.Commerce.utils.sorting.SortingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Product Management", description = "APIs for managing products")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final IProductService productService;
    private final SortingService sortingService;

    public ProductController(IProductService productService, SortingService sortingService) {
        this.productService = productService;
        this.sortingService = sortingService;
    }

    @Operation(summary = "Add a new product")
    @RequiresRole(UserRole.ADMIN)
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> addProduct(@Valid @RequestBody AddProductDTO request){
        ProductResponseDTO product = productService.addProduct(request);
        ApiResponse<ProductResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product added successfully", product);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Add multiple products")
    @RequiresRole(UserRole.ADMIN)
    @PostMapping("/add/bulk")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> addProducts(@Valid @RequestBody List<AddProductDTO> requests){
        List<ProductResponseDTO> products = requests.stream()
            .map(productService::addProduct)
            .toList();
        ApiResponse<List<ProductResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), 
            products.size() + " products added successfully", products);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get all products")
    @GetMapping("/public/all")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponseDTO>>> getAllProducts(
            @RequestAttribute(value = "authenticatedUserRole", required = false) String userRole,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "ascending", defaultValue = "true") boolean ascending,
            @RequestParam(value = "algorithm", defaultValue = "QUICKSORT") String algorithm
    ){
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<ProductResponseDTO> products;
        
        boolean isAdmin = "ADMIN".equals(userRole);
        
        if (categoryId != null) {
            products = productService.getProductsByCategory(categoryId, pageable, isAdmin);
        } else {
            products = productService.getAllProducts(pageable, isAdmin);
        }
        
        List<ProductResponseDTO> productList = products.getContent();
        
        // Apply custom sorting if sortBy is specified
        if (sortBy != null) {
            try {
                SortingService.ProductSortField field = SortingService.ProductSortField.valueOf(sortBy.toUpperCase());
                SortingService.SortAlgorithm algo = SortingService.SortAlgorithm.valueOf(algorithm.toUpperCase());
                sortingService.sortProducts(productList, field, ascending, algo);
            } catch (IllegalArgumentException e) {
                // Invalid sortBy or algorithm, ignore and return unsorted
            }
        }
        
        PagedResponse<ProductResponseDTO> pagedResponse = new PagedResponse<>(
                productList,
                products.getNumber(),
                (int) products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );
        ApiResponse<PagedResponse<ProductResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Products fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable Long id){
        ProductResponseDTO product = productService.getProductById(id);
        ApiResponse<ProductResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product fetched successfully", product);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update a product")
    @RequiresRole(UserRole.ADMIN)
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateProductDTO request){
        ProductResponseDTO updatedProduct = productService.updateProduct(id, request);
        ApiResponse<ProductResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product updated successfully", updatedProduct);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Delete a product")
    @RequiresRole(UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
    }
}
