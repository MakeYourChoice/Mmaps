/*
 * Copyright 2016 Shen Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dg.shenm233.api.maps.overlay;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.WalkStep;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.util.AMapUtils;

public class BusRouteOverlayS extends RouteOverlayS {
    private BusPath mPath;

    private LatLng mLastPoint; // BusPath的最后一个地理位置点

    public BusRouteOverlayS(Context context, AMap aMap, BusPath busPath,
                            LatLonPoint start, LatLonPoint end) {
        super(context, aMap, start, end);
        mPath = busPath;
    }


    @Override
    public void addToMap() {
        List<BusStep> stepList = mPath.getSteps();
        final int length = stepList.size();
        for (int i = 0; i < length; i++) {
            final BusStep step = stepList.get(i);
            RouteBusLineItem busItem = step.getBusLine();
            RouteBusWalkItem walkItem = step.getWalk();

            if (i < length - 1) {
                final BusStep nextStep = stepList.get(i + 1);
                RouteBusWalkItem nextWalkItem = nextStep.getWalk();
                RouteBusLineItem nextBusItem = nextStep.getBusLine();

                if (walkItem != null && busItem != null) {
                    linkWalkAndBus(walkItem, busItem);
                }

                if (busItem != null && nextWalkItem != null) {
                    linkBusAndWalk(busItem, nextWalkItem);
                }

                // 公交车换乘
                if (busItem != null && nextWalkItem == null && nextBusItem != null) {
                    switchBus(busItem, nextBusItem);
                }
            }

            if (walkItem != null && walkItem.getSteps().size() > 0) {
                addWalkPolyline(walkItem);
            } else if (busItem == null) {
                addWalkPolyline(mLastPoint, endPoint);
            }

            if (busItem != null) {
                addBusPolyline(busItem);
            }
        }

        addStartAndEndMarker();
    }

    private void linkWalkAndBus(RouteBusWalkItem walkItem, RouteBusLineItem busItem) {
        LatLonPoint walkEndm = getWalkEnd(walkItem);
        LatLonPoint busStartm = getBusStart(busItem);

        if (walkEndm == null || busStartm == null) {
            return;
        }

        LatLng walkEnd = AMapUtils.convertToLatLng(walkEndm);
        LatLng busStart = AMapUtils.convertToLatLng(busStartm);

        if (!walkEnd.equals(busStart)) {
            addWalkPolyline(walkEnd, busStart);
        }
    }

    private void linkBusAndWalk(RouteBusLineItem busItem, RouteBusWalkItem walkItem) {
        LatLonPoint busEndm = getBusEnd(busItem);
        LatLonPoint walkStartm = getWalkStart(walkItem);

        if (busEndm == null || walkStartm == null) {
            return;
        }

        LatLng busEnd = AMapUtils.convertToLatLng(busEndm);
        LatLng walkStart = AMapUtils.convertToLatLng(walkStartm);

        if (!busEnd.equals(walkStart)) {
            addWalkPolyline(busEnd, walkStart);
        }
    }

    private void switchBus(RouteBusLineItem a, RouteBusLineItem b) {
        LatLonPoint busEndm = getBusEnd(a);
        LatLonPoint busStartm = getBusStart(b);

        if (busEndm == null || busStartm == null) {
            return;
        }

        LatLng aEnd = AMapUtils.convertToLatLng(busEndm);
        LatLng bStart = AMapUtils.convertToLatLng(busStartm);

        if (!aEnd.equals(bStart)) {
            drawLineWithDot(aEnd, bStart);
        }
    }

    private void drawLineWithDot(LatLng start, LatLng end) {
        addPolyLine(new PolylineOptions().add(start, end)
                .width(getRouteWidth())
                .color(getBusColor())
                .setDottedLine(true)
        );
    }

    private void addWalkPolyline(LatLng a, LatLng b) {
        addPolyLine(new PolylineOptions().add(a, b)
                .color(getWalkColor())
                .width(getRouteWidth())
        );
    }

    private void addWalkPolyline(RouteBusWalkItem walkItem) {
        List<LatLng> pointList = new ArrayList<>();

        List<WalkStep> stepList = walkItem.getSteps();
        final int length = stepList.size();
        for (int i = 0; i < length; i++) {
            final WalkStep step = stepList.get(i);
            List<LatLonPoint> latLonPoints = step.getPolyline();
            for (LatLonPoint point : latLonPoints) {
                LatLng p = AMapUtils.convertToLatLng(point);
                pointList.add(p);
                mLastPoint = p;
            }
        }

        addPolyLine(new PolylineOptions().addAll(pointList)
                .color(getWalkColor())
                .width(getRouteWidth())
        );
    }

    private void addBusPolyline(RouteBusLineItem busLineItem) {
        List<LatLng> list = AMapUtils.convertToLatLng(busLineItem.getPolyline());

        addPolyLine(new PolylineOptions().addAll(list)
                .color(getBusColor())
                .width(getRouteWidth())
        );
    }

    private LatLonPoint getWalkStart(RouteBusWalkItem walkItem) {
        List<WalkStep> walkSteps = walkItem.getSteps();
        if (walkSteps.size() == 0) {
            return null;
        }
        List<LatLonPoint> list = walkSteps.get(0).getPolyline();
        return list.get(0);
    }

    private LatLonPoint getWalkEnd(RouteBusWalkItem walkItem) {
        List<WalkStep> walkSteps = walkItem.getSteps();
        if (walkSteps.size() == 0) {
            return null;
        }
        List<LatLonPoint> list = walkSteps.get(walkSteps.size() - 1).getPolyline();
        return list.get(list.size() - 1);
    }

    private LatLonPoint getBusStart(RouteBusLineItem busLineItem) {
        List<LatLonPoint> list = busLineItem.getPolyline();
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    private LatLonPoint getBusEnd(RouteBusLineItem busLineItem) {
        List<LatLonPoint> list = busLineItem.getPolyline();
        if (list.size() == 0) {
            return null;
        }
        return list.get(list.size() - 1);
    }
}
