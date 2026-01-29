package com.example.Commerce.Services;

import com.example.Commerce.DTOs.AddProductDTO;
import com.example.Commerce.DTOs.ProductResponseDTO;
import com.example.Commerce.DTOs.UpdateProductDTO;
import com.example.Commerce.Entities.CategoryEntity;
import com.example.Commerce.Entities.InventoryEntity;
import com.example.Commerce.Entities.ProductEntity;
import com.example.Commerce.Mappers.ProductMapper;
import com.example.Commerce.interfaces.ICategoryRepository;
import com.example.Commerce.interfaces.IInventoryRepository;
import com.example.Commerce.interfaces.IProductRepository;
import com.example.Commerce.interfaces.IProductService;
import com.example.Commerce.cache.CacheManager;
import com.example.Commerce.errorHandlers.ConstraintViolationException;
import com.example.Commerce.errorHandlers.ResourceAlreadyExists;
import com.example.Commerce.errorHandlers.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ProductService implements IProductService { 
    private final IProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ICategoryRepository categoryRepository;
    private final IInventoryRepository inventoryRepository;
    private final CacheManager cacheManager;

    public ProductService(IProductRepository productRepository, ProductMapper productMapper, ICategoryRepository categoryRepository, IInventoryRepository inventoryRepository, CacheManager cacheManager) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
        this.cacheManager = cacheManager;
    }

    public ProductResponseDTO addProduct(AddProductDTO addProductDTO){
        if(productRepository.existsByNameIgnoreCase(addProductDTO.getName())){
            throw new ResourceAlreadyExists("Product already exists");
        }
        CategoryEntity category = categoryRepository.findById(addProductDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + addProductDTO.getCategoryId()));
        ProductEntity productEntity = productMapper.toEntity(addProductDTO);
        productEntity.setCategoryId(addProductDTO.getCategoryId());
        ProductEntity savedProduct = productRepository.save(productEntity);
        ProductResponseDTO response =  productMapper.toResponseDTO(savedProduct);
        response.setCategoryName(category.getName());
        
        // Set quantity from inventory if exists
        inventoryRepository.findByProductId(savedProduct.getId())
                .ifPresent(inventory -> response.setQuantity(inventory.getQuantity()));
        
        return response;
    }

    public Page<ProductResponseDTO> getAllProducts(Pageable pageable, boolean isAdmin){
        Page<ProductEntity> productPage = isAdmin ? 
            productRepository.findAll(pageable) : 
            productRepository.findAllWithInventory(pageable);
            
        return productPage.map(product -> {
            ProductResponseDTO response = productMapper.toResponseDTO(product);
            
            CategoryEntity category = cacheManager.get("category:" + product.getCategoryId(), () ->
                categoryRepository.findById(product.getCategoryId())
                    .orElse(null)
            );
            if (category != null) {
                response.setCategoryName(category.getName());
            }
            
            Integer quantity = cacheManager.get("inventory:quantity:" + product.getId(), () -> 
                inventoryRepository.findByProductId(product.getId())
                    .map(InventoryEntity::getQuantity)
                    .orElse(0)
            );
            response.setQuantity(quantity);
            return response;
        });
    }

    public Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable, boolean isAdmin){
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
        
        Page<ProductEntity> productPage = isAdmin ? 
            productRepository.findByCategoryId(categoryId, pageable) : 
            productRepository.findByCategoryIdWithInventory(categoryId, pageable);
            
        return productPage.map(product -> {
            ProductResponseDTO response = productMapper.toResponseDTO(product);
            
            CategoryEntity category = cacheManager.get("category:" + product.getCategoryId(), () ->
                categoryRepository.findById(product.getCategoryId())
                    .orElse(null)
            );
            if (category != null) {
                response.setCategoryName(category.getName());
            }
            
            Integer quantity = cacheManager.get("inventory:quantity:" + product.getId(), () -> 
                inventoryRepository.findByProductId(product.getId())
                    .map(InventoryEntity::getQuantity)
                    .orElse(0)
            );
            response.setQuantity(quantity);
            return response;
        });
    }

    public ProductResponseDTO getProductById(Long id){
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        ProductResponseDTO response = productMapper.toResponseDTO(product);
        
        categoryRepository.findById(product.getCategoryId())
                .ifPresent(category -> response.setCategoryName(category.getName()));
        
        inventoryRepository.findByProductId(product.getId())
                .ifPresent(inventory -> response.setQuantity(inventory.getQuantity()));
        
        return response;
    }

    public ProductResponseDTO updateProduct(Long id, UpdateProductDTO updateProductDTO){
        ProductEntity existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        if(updateProductDTO.getName() != null && 
           !existingProduct.getName().equalsIgnoreCase(updateProductDTO.getName()) && 
           productRepository.existsByNameIgnoreCase(updateProductDTO.getName())){
            throw new ResourceAlreadyExists("Product with name '" + updateProductDTO.getName() + "' already exists");
        }

        if(updateProductDTO.getCategoryId() != null) {
            categoryRepository.findById(updateProductDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + updateProductDTO.getCategoryId()));
            existingProduct.setCategoryId(updateProductDTO.getCategoryId());
        }

        productMapper.updateEntity(updateProductDTO, existingProduct);
        ProductEntity updatedProduct = productRepository.save(existingProduct);
        
        cacheManager.invalidate("product:" + id);
        cacheManager.invalidate("inventory:quantity:" + id);
        
        ProductResponseDTO response = productMapper.toResponseDTO(updatedProduct);
        
        categoryRepository.findById(updatedProduct.getCategoryId())
                .ifPresent(category -> response.setCategoryName(category.getName()));
        
        inventoryRepository.findByProductId(updatedProduct.getId())
                .ifPresent(inventory -> response.setQuantity(inventory.getQuantity()));
        
        return response;
    }

    public java.util.List<ProductResponseDTO> getAllProductsList() {
        return productRepository.findAllWithInventory().stream().map(product -> {
            ProductResponseDTO response = productMapper.toResponseDTO(product);
            Integer quantity = cacheManager.get("inventory:quantity:" + product.getId(), () -> 
                inventoryRepository.findByProductId(product.getId())
                    .map(InventoryEntity::getQuantity)
                    .orElse(null)
            );
            response.setQuantity(quantity);
            return response;
        }).toList();
    }

    public void deleteProduct(Long id){
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        try {
            productRepository.delete(product);
            cacheManager.invalidate("product:" + id);
            cacheManager.invalidate("inventory:quantity:" + id);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("foreign key constraint")) {
            throw new ConstraintViolationException(
                    "Cannot delete product. It is being used in orders or inventory. Please remove related records first.");
            }
            throw ex;
        }
    }
}

