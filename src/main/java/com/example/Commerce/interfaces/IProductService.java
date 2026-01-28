package com.example.Commerce.interfaces;

import com.example.Commerce.DTOs.AddProductDTO;
import com.example.Commerce.DTOs.ProductResponseDTO;
import com.example.Commerce.DTOs.UpdateProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IProductService {
    ProductResponseDTO addProduct(AddProductDTO addProductDTO);
    Page<ProductResponseDTO> getAllProducts(Pageable pageable);
    Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable);
    ProductResponseDTO getProductById(Long id);
    ProductResponseDTO updateProduct(Long id, UpdateProductDTO updateProductDTO);
    List<ProductResponseDTO> getAllProductsList();
    void deleteProduct(Long id);
}
