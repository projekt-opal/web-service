package org.dice_research.opal.webservice.model.entity;

import lombok.Data;

@Data
public class Spatial {
    private GeoLocation geometry;
    private String tag;
}

@Data
class GeoLocation {
    private double lat;
    private double lon;
}
