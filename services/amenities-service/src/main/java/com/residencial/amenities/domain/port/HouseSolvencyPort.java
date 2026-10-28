package com.residencial.amenities.domain.port;

import java.util.UUID;

public interface HouseSolvencyPort {
    boolean isHouseSolvent(UUID houseId);
}
