package com.veeva.vault.custom.udc;

import com.veeva.vault.sdk.api.core.UserDefinedClassInfo;

import java.math.BigDecimal;

/*
 * The BicyclePartData is used to store the bicycle part name, the record id, and the bicycle part quantity.
 */
@UserDefinedClassInfo
public class BicyclePartData {
    private final String bicyclePartName;
    private final Boolean idExists;
    private final String id;
    private final BigDecimal quantity;

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BicyclePartData(String bicyclePartName, Boolean idExists, String id, BigDecimal quantity) {
        this.bicyclePartName = bicyclePartName;
        this.idExists = idExists;
        this.id = id;
        this.quantity = quantity;
    }

    public String getBicyclePartName() {
        return bicyclePartName;
    }

    public Boolean getIdExists() {
        return idExists;
    }

    public String getId() {
        return id;
    }

}
