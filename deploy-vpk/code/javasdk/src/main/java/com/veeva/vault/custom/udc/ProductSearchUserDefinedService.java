package com.veeva.vault.custom.udc;

import com.veeva.vault.sdk.api.core.UserDefinedService;
import com.veeva.vault.sdk.api.core.UserDefinedServiceInfo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;



/**
 * ProductSearchService is an Interface that uses UserDefinedService.
 * ProductSearchService Implementations must implement the doesProductExist method and the reduceProductQuantity method.
 * UserDefinedService is a Vault SDK API that allow reusable logic to be wrapped into a service that can be used by other Vault Java SDK code.
 * Vault SDK code such as triggers, actions or user-defined classes can use a a user-defined service by locating it using ServiceLocator.
 * Unlike user-defined classes, user-defined services are stateless; nothing is retained beyond the service method execution.
 * The Vault SDK Documentation regarding User Defined Services can found here: https://developer.veevavault.com/sdk/#user-defined-services
 */
@UserDefinedServiceInfo
public interface ProductSearchUserDefinedService extends UserDefinedService {
     Map<String, BicyclePartData> doesProductExist(Map<String, String> productAndManufacturerMap);
     void reduceProductQuantity(Collection<BicyclePartData> bicyclePartData, Map<String, BigDecimal> orderQuantityMap);
}
