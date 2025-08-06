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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public List<ProductSnapshotResult> getProductsForSnapshot(List<Long> ids){
        return productRepository.getProductsForSnapshot(ids);
    }

    @Transactional(readOnly = true)
    public List<ProductModel> getListByIds(List<Long> ids){
        return productRepository.getProductsByIdIn(ids);
    }

    @Transactional(readOnly = true)
    public Page<ProductModel> getProductsWithPageAndSort(int page, int size, OrderType orderType){
        return productRepository.findAllByPaging(page, size, orderType);
    }


    public ProductModel save(String name, Long stock, Long price,
                             ProductStatus productStatus, Long brandId){
        ProductModel productModel = new ProductModel(name, stock, price, productStatus, brandId);
        return productRepository.saveProduct(productModel);

    }

    public void checkExistProduct(Long id){
        Boolean existProduct = productRepository.existProduct(id);

        if(!existProduct){
            throw new CoreException(ErrorType.NOT_FOUND, "상품 정보를 찾을 수 없습니다.");
        }
    }


    public ProductModel adjustLikeCount(ProductModel productModel, LikeToggleResult like){
        productModel.applyLikeToggle(like);

        return productRepository.saveProduct(productModel);

    }


    public void increaseLikeCount(Long productId){
        ProductModel productModel = productRepository.getProductForUpdate(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품 정보를 찾을 수 없습니다."));
        productModel.increaseLikeCount();

    }

    public void decreaseLikeCount(Long productId){
        ProductModel productModel = productRepository.getProductForUpdate(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품 정보를 찾을 수 없습니다."));
        productModel.decreaseLikeCount();

    }


    @Transactional
    public void deductStocks(ProductCommand.DeductStocks command){

        //데드락 방지를 위해 정렬
        List<Long> sortedProductIds = command.productQuantities().stream()
                .map(ProductCommand.ProductQuantity::productId)
                .distinct()
                .sorted()
                .toList();

        List<ProductModel> products = productRepository.getProductsByIdInForUpdate(sortedProductIds);


        Map<Long, ProductModel> productModelMap = products.stream()
                .collect(Collectors.toMap(ProductModel::getId, Function.identity()));

        for(ProductCommand.ProductQuantity quantity : command.productQuantities()){
            ProductModel productModel = productModelMap.get(quantity.productId());
            if (productModel == null) {
                throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다: " + quantity.productId());
            }


            productModel.deduct(quantity.quantity());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }



        }
//        productRepository.saveProducts(products);


    }





}
