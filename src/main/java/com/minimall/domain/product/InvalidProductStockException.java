    package com.minimall.domain.product;

    import com.minimall.domain.exception.DomainExceptionMessage;
    import lombok.Getter;

    @Getter
    public class InvalidProductStockException extends RuntimeException {

        public enum Reason {
            NEGATIVE,      //파라미터가 음수
            REQUIRED,      //재고 null 금지
            INSUFFICIENT,  //재고 부족
            REQUIRED_POSITIVE
        }

        private static final String PARAM_NAME = "product.stock";
        private final Reason reason;
        private final Integer requested;
        private final Integer available;

        private InvalidProductStockException(Reason reason, String message, Integer requested, Integer available) {
            super(message);
            this.reason = reason;
            this.requested = requested;
            this.available = available;
        }


        //== Static Factory Methods ==//
        public static InvalidProductStockException negative(int requested) {
            return new InvalidProductStockException(
                    Reason.NEGATIVE,
                    DomainExceptionMessage.PARAM_CANNOT_BE_NEGATIVE.text(PARAM_NAME, requested),
                    requested, null
            );
        }

        public static InvalidProductStockException required() {
            return new InvalidProductStockException(
                    Reason.REQUIRED,
                    DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(PARAM_NAME),
                    null, null);
        }

        public static InvalidProductStockException insufficient(int requested, int available) {
            return new InvalidProductStockException(
                    Reason.INSUFFICIENT,
                    ProductMessage.STOCK_INSUFFICIENT.text(requested, available),
                    requested, available
            );
        }

        public static InvalidProductStockException requirePositive(int requested) {
            return new InvalidProductStockException(
                    Reason.REQUIRED_POSITIVE,
                    DomainExceptionMessage.PARAM_REQUIRE_POSITIVE.text(PARAM_NAME, requested),
                    requested, null
            );
        }
    }