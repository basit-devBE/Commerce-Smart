package com.example.commerce.interfaces;

import com.example.commerce.dtos.AddProductDTO;
import com.example.commerce.dtos.ProductResponseDTO;
import com.example.commerce.dtos.UpdateProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService {
    ProductResponseDTO addProduct(AddProductDTO addProductDTO);

    Page<ProductResponseDTO> getAllProducts(Pageable pageable, boolean isAdmin);

    Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable, boolean isAdmin);

    ProductResponseDTO getProductById(Long id);

    ProductResponseDTO updateProduct(Long id, UpdateProductDTO updateProductDTO);

    List<ProductResponseDTO> getAllProductsList();

    void deleteProduct(Long id);
}
