package com.example.demo.nest.util;

import com.example.demo.de.lighti.clipper.*;
import com.example.demo.nest.data.NestPath;
import com.example.demo.nest.data.Segment;
import com.example.demo.nest.util.coor.ClipperCoor;
import com.example.demo.nest.util.coor.NestCoor;

import java.util.ArrayList;
import java.util.List;


public class CommonUtil {


    public static NestPath Path2NestPath(Path path) {
        NestPath nestPath = new NestPath();
        for (int i = 0; i < path.size(); i++) {
            Point.LongPoint lp = path.get(i);
            NestCoor coor = CommonUtil.toNestCoor(lp.getX(), lp.getY());
            nestPath.add(new Segment(coor.getX(), coor.getY()));
        }
        return nestPath;
    }

    public static Path NestPath2Path(NestPath nestPath) {
        Path path = new Path();
        for (Segment s : nestPath.getSegments()) {
            ClipperCoor coor = CommonUtil.toClipperCoor(s.getX(), s.getY());
            Point.LongPoint lp = new Point.LongPoint(coor.getX(), coor.getY());
            path.add(lp);
        }
        return path;
    }

    public static ClipperCoor toClipperCoor(double x, double y) {
        return new ClipperCoor((long) (x * Config.CLIIPER_SCALE), (long) (y * Config.CLIIPER_SCALE));
    }

    public static NestCoor toNestCoor(long x, long y) {
        return new NestCoor(((double) x / Config.CLIIPER_SCALE), ((double) y / Config.CLIIPER_SCALE));
    }


    private static void addPoint(long x, long y, Path path) {
        Point.LongPoint ip = new Point.LongPoint(x, y);
        path.add(ip);
    }


    public static void ChangePosition(NestPath binPath, List<NestPath> polys) {

    }

    public static int toTree(List<NestPath> list, int idstart) {
        List<NestPath> parents = new ArrayList<NestPath>();
        int id = idstart;

        for (int i = 0; i < list.size(); i++) {
            NestPath p = list.get(i);
            boolean isChild = false;
            for (int j = 0; j < list.size(); j++) {
                if (j == i) {
                    continue;
                }
                if (GeometryUtil.pointInPolygon(p.getSegments().get(0), list.get(j)) == true) {
                    list.get(j).getChildren().add(p);
                    p.setParent(list.get(j));
                    isChild = true;
                    break;
                }
            }
            if (!isChild) {
                parents.add(p);
            }
        }

        for (int i = 0; i < list.size(); i++) {
            if (parents.indexOf(list.get(i)) < 0) {
                list.remove(i);
                i--;
            }
        }

        for (int i = 0; i < parents.size(); i++) {
            parents.get(i).setId(id);
            id++;
        }

        for (int i = 0; i < parents.size(); i++) {
            if (parents.get(i).getChildren().size() > 0) {
                id = toTree(parents.get(i).getChildren(), id);
            }
        }
        return id;
    }

    public static NestPath clipperToNestPath(Path polygon) {
        NestPath normal = new NestPath();
        for (int i = 0; i < polygon.size(); i++) {
            NestCoor nestCoor = toNestCoor(polygon.get(i).getX(), polygon.get(i).getY());
            normal.add(new Segment(nestCoor.getX(), nestCoor.getY()));
        }
        return normal;
    }

    public static void offsetTree(List<NestPath> t, double offset) {
        for (int i = 0; i < t.size(); i++) {
            List<NestPath> offsetPaths = polygonOffset(t.get(i), offset);
            if (offsetPaths.size() == 1) {
                t.get(i).clear();
                NestPath from = offsetPaths.get(0);

                for (Segment s : from.getSegments()) {
                    t.get(i).add(s);
                }
            }
            if (t.get(i).getChildren().size() > 0) {

                offsetTree(t.get(i).getChildren(), -offset);
            }
        }
    }

    public static List<NestPath> polygonOffset(NestPath polygon, double offset) {
        List<NestPath> result = new ArrayList<NestPath>();
        if (offset == 0 || GeometryUtil.almostEqual(offset, 0)) {

            return result;
        }
        Path p = new Path();
        for (Segment s : polygon.getSegments()) {
            ClipperCoor cc = CommonUtil.toClipperCoor(s.getX(), s.getY());
            p.add(new Point.LongPoint(cc.getX(), cc.getY()));
        }

        int miterLimit = 2;
        ClipperOffset co = new ClipperOffset(miterLimit, polygon.config.CURVE_TOLERANCE * Config.CLIIPER_SCALE);
        co.addPath(p, Clipper.JoinType.ROUND, Clipper.EndType.CLOSED_POLYGON);

        Paths newpaths = new Paths();
        co.execute(newpaths, offset * Config.CLIIPER_SCALE);


        for (int i = 0; i < newpaths.size(); i++) {
            result.add(CommonUtil.clipperToNestPath(newpaths.get(i)));
        }

        if (offset > 0) {
            NestPath from = result.get(0);
            if (GeometryUtil.polygonArea(from) > 0) {
                from.reverse();
            }
            from.add(from.get(0));
            from.getSegments().remove(0);
        }


        return result;
    }

}
