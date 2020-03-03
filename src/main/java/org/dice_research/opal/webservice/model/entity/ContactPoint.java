package org.dice_research.opal.webservice.model.entity;

import lombok.Data;

@Data
public class ContactPoint {
    private String email;
    private String name;
    private String address;
    private String phone;
}
