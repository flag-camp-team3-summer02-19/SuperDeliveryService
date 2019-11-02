package SuperDelivery.service.idm;

import SuperDelivery.service.idm.constants.DeliveryServiceInfo;
import SuperDelivery.service.idm.models.*;
import SuperDelivery.service.idm.models.PackageInfo.PackageInfoBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;

import java.math.BigDecimal;
import java.time.Instant;

//import static SuperDelivery.service.idm.core.HelperXuan.isWorkerAvailable;


public class Test {

    public static void test() {
        try {
            GeoApiContext context = IDMService.getGeoApiContext();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

//            LocationLatLon loc = HelperXuan.getLatLon("1600 Amphitheatre Parkway Mountain View, CA 94043");
//            System.out.println(loc.getLat());
//            System.out.println(loc.getLon());




//            DirectionsResult result =DirectionsApi.newRequest(context)
//                    .origin("Fisherman's Wharf, San Francisco, CA")
//                    .destination("Union Square, San Francisco, CA")
//                    .mode(TravelMode.DRIVING)
//                    .await();

//            DistanceMatrix result = DistanceMatrixApi.newRequest(context)
//                    .origins("Fisherman's Wharf, San Francisco, CA")
//                    .destinations("Union Square, San Francisco, CA")
//                    .mode(TravelMode.WALKING)
//                    .await();
//
//            System.out.println(gson.toJson(result));


//            LocationLatLon loc = new LocationLatLonBuilder().setLat(BigDecimal.valueOf(37.764792))
//                    .setLon(BigDecimal.valueOf(-122.400145)).build();
//
//            Warehouse wh = findNearestWarehouse(loc, WorkerType.ROBOT);
//            System.out.println(wh.getDbName());

//            isWorkerAvailable(Warehouse.WAREHOUSE1, WorkerType.ROBOT);

//            PackageInfo pkgInfo = new PackageInfoBuilder().setPkgLength(10).
//                    setPkgWidth(5).setPkgHeight(2).setPkgWeight(22).setPkgFrom("aaaa").
//                    setPkgTo("bbbbbb")





        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
