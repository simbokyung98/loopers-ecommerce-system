package com.loopers.domain.product;

import com.loopers.domain.Like.LikeToggleResult;
import com.loopers.interfaces.api.product.OrderType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ProductModel get(Long id){
        Optional<ProductModel> optionalProductModel =
                productRepository.getProduct(id);
        if(optionalProductModel.isEmpty()){
            throw new CoreException(ErrorType.NOT_FOUND, "상품 정보를 찾을 수 없습니다.");
        }
        return optionalProductModel.get();
    }

    @Transactional(readOnly = true)
    public Page<ProductModel> getListWithPaging(int page, int size, OrderType orderType){
        return productRepository.findAllByPaging(page, size, orderType);
    }


    public ProductModel save(String name, Long stock, Long price,
                             ProductStatus productStatus, Long brandId){
        ProductModel productModel = new ProductModel(name, stock, price, productStatus, brandId);
        return productRepository.save(productModel);

    }

    public void checkExistProduct(Long id){
        Boolean existProduct = productRepository.existProduct(id);

        if(!existProduct){
            throw new CoreException(ErrorType.NOT_FOUND, "상품 정보를 찾을 수 없습니다.");
        }
    }

    public ProductModel adjustLikeCount(ProductModel productModel, LikeToggleResult like){
        productModel.applyLikeToggle(like);

        return productRepository.save(productModel);

    }

    public List<ProductModel> getListByIds(List<Long> ids){
        return productRepository.findByIdIn(ids);
    }





}
