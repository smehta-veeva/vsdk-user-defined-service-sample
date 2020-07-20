package com.veeva.vault.custom.triggers;

import com.veeva.vault.custom.udc.BicyclePartData;
import com.veeva.vault.custom.udc.ProductSearchUserDefinedService;
import com.veeva.vault.sdk.api.core.*;
import com.veeva.vault.sdk.api.data.*;
import com.veeva.vault.sdk.api.picklist.Picklist;
import com.veeva.vault.sdk.api.picklist.PicklistService;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * This class annotation (@RecordTriggerInfo) indicates that this class is a record trigger.
 * It specifies the object that this trigger will run on(bicycle_part_order__c), the events it will run on(BEFORE_INSERT) and the order(1st).
 */
@RecordTriggerInfo(object = "bicycle_part_order__c", events = { RecordEvent.BEFORE_INSERT, RecordEvent.BEFORE_UPDATE }, order= TriggerOrder.NUMBER_1)
public class UserDefinedServiceSampleOrderFieldValueValidation implements RecordTrigger {
    public void execute(RecordTriggerContext recordTriggerContext) {
        /*
         * Get an instance of the Log Service used to log errors and exceptions.
         * Log Service Java Doc can be found here: https://repo.veevavault.com/javadoc/vault-sdk-api/19.3.5/docs/api/index.html
         */
        LogService logService = ServiceLocator.locate(LogService.class);
        /*
         * Get an instance of the User Defined Service we created.
         * User defined Services are used similarly to Vault SDK services.
         * User Defined Services don't contribute to the memory limit after the method has exited.
         */
        ProductSearchUserDefinedService productSearchUserDefinedService = ServiceLocator.locate(ProductSearchUserDefinedService.class);

        /*
         * Get an instance of the PickList Service used to retrieve picklist values.
         * More information about hor to retrieve picklist values can be found here:
         * https://repo.veevavault.com/javadoc/vault-sdk-api/20.1.0/docs/api/com/veeva/vault/sdk/api/picklist/package-summary.html
         */
        PicklistService picklistService = ServiceLocator.locate(PicklistService.class);
        // Picklist of bicycle parts in bicycle part orders
        Picklist bicyclePartPicklist = picklistService.getPicklist("bicycle_part__c");
        // Picklist of bicycle part manufacturers in bicycle part orders
        Picklist bicyclePartManufacturersPicklist = picklistService.getPicklist("bicycle_part_manufacturer__c");

        @SuppressWarnings("unchecked")
        Map<String, String> partAndManufacturerMap = VaultCollections.newMap();
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> orderQuantityMap = VaultCollections.newMap();

        recordTriggerContext.getRecordChanges().forEach(recordChange -> {

            // Initialize part name and manufacture name strings
            String bicyclePartName = "";
            String bicyclePartManufacturerName = "";

            // Get the bicycle parts pick list values
            List<String> bicyclePartsListValues = recordChange.getNew().getValue("bicycle_part__c", ValueType.PICKLIST_VALUES);

            // Get the name(label) of the bicycle part using the bicyclePartPickList
            if (bicyclePartsListValues != null && !bicyclePartsListValues.isEmpty()) {
                // Get single-picklist value name selected by user
                String partPicklistValueName = bicyclePartsListValues.get(0);
                // Get picklist value label
                bicyclePartName = bicyclePartPicklist.getPicklistValue(partPicklistValueName).getLabel();
            }

            // Get the name(label) of the bicycle part manufacturer using the bicyclePartManufacturerPickList
            List<String> bicyclePartManufacturersListValues = recordChange.getNew().getValue("bicycle_part_manufacturer__c", ValueType.PICKLIST_VALUES);
            if (bicyclePartManufacturersListValues != null && !bicyclePartManufacturersListValues.isEmpty()) {
                // Get single-picklist value name selected by user
                String partManufacturerPicklistValueName = bicyclePartManufacturersListValues.get(0);
                // Get picklist value label
                bicyclePartManufacturerName = bicyclePartManufacturersPicklist.getPicklistValue(partManufacturerPicklistValueName).getLabel();
            }

            // If the bicycle part name or the manufacturer name are null or empty, then throw an error.
            if((bicyclePartName == null || bicyclePartName.isEmpty()) ||
                    (bicyclePartManufacturerName == null || bicyclePartManufacturerName.isEmpty())) {
                recordChange.setError("INVALID_ARGUMENT", "Product with specified manufacturer does not exist.");
            }

            // Get the order quantity
            BigDecimal orderQuantity = recordChange.getNew().getValue("order_quantity__c", ValueType.NUMBER);

            // Add part name, part manufacturer name and order quantity to maps
            partAndManufacturerMap.put("'" + bicyclePartName + "'", "'" + bicyclePartManufacturerName + "'");
            orderQuantityMap.put(bicyclePartName, orderQuantity);
        });
        logService.logResourceUsage("Before doesProductExist Method Call: ");
        Map<String, BicyclePartData> doProductsExist = productSearchUserDefinedService.doesProductExist(partAndManufacturerMap);
        logService.logResourceUsage("After doesProductExist Method Call: " );

        // Validate that all user entered bicycle part name were found, if not setError on the record change.
        for (RecordChange recordChange : recordTriggerContext.getRecordChanges()) {
            // If no results were found, all user inputted parts and manufacturer combinations are wrong.
            if (doProductsExist == null) {
                recordChange.setError("INVALID_ARGUMENT", "Product with specified manufacturer does not exist.");
                continue;
            }

            // Get the bicycle parts pick list values
            List<String> bicyclePartsListValues = recordChange.getNew().getValue("bicycle_part__c", ValueType.PICKLIST_VALUES);

            String bicyclePartName = "";
            // Get the name(label) of the bicycle part using the bicyclePartPickList
            if (bicyclePartsListValues != null && !bicyclePartsListValues.isEmpty()) {
                // Get single-picklist value name selected by user
                String partPicklistValueName = bicyclePartsListValues.get(0);
                // Get picklist value label
                bicyclePartName = bicyclePartPicklist.getPicklistValue(partPicklistValueName).getLabel();
            }

            if (bicyclePartName != null && !doProductsExist.get(bicyclePartName).getIdExists()) { // If Id of bicycle part is found then it exists
                // setError if the user specified bicycle part's record was not found.
                recordChange.setError("INVALID_ARGUMENT", "Product with specified manufacturer does not exist.");
            }
        }

        // Reduce the quantity for the bicycle parts that we are creating an order for.
        if(doProductsExist != null) {
            logService.logResourceUsage("Before reduceProductQuantity Method Call: ");
            productSearchUserDefinedService.reduceProductQuantity(doProductsExist.values(), orderQuantityMap);
            logService.logResourceUsage("After reduceProductQuantity Method Call: ");
        }
        logService.logResourceUsage("End: ");
    }
}
