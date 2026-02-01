package com.example.demo.nest.util;

import com.example.demo.de.lighti.clipper.Clipper;
import com.example.demo.de.lighti.clipper.DefaultClipper;
import com.example.demo.de.lighti.clipper.Path;
import com.example.demo.de.lighti.clipper.Paths;
import com.example.demo.nest.data.*;
import com.example.demo.nest.data.Vector;
import com.example.demo.share.Piece;

import java.util.*;

import static java.lang.Math.*;
import static com.example.demo.share.RePlacement.scaleUp2ClipperCoordinates;
import static com.example.demo.share.RePlacement.toNestCoordinates;

public class GeometryUtil {
    private static double TOL = Math.pow(10,-2);

    public static boolean almostEqual(double a, double b ){
        return Math.abs(a-b)<TOL;
    }

    public static boolean almostEqual(double a , double b  , double tolerance){
        return Math.abs(a-b)<tolerance;
    }

    public static double angleBetweenLines(Segment A, Segment B, Segment C, Segment D) {
        double angle = 0.0;
        double k1 = (B.getY() - A.getY()) / (B.getX() - A.getX());
        double k2 = (D.getY() - C.getY()) / (D.getX() - C.getX());
        if (Double.isInfinite(k1) && Double.isInfinite(k2)) {
            angle = 0.0;
        } else if (Double.isInfinite(k1)) {
            double k3 = (D.getY() - C.getY()) / (D.getX() - C.getX() + 0.00001);
            angle = Math.atan(k3);
        } else if (Double.isInfinite(k2)) {
            double k3 = (B.getY() - A.getY()) / (B.getX() - A.getX() + 0.00001);
            angle = Math.atan(k3);
        } else {
            double dk = Math.atan(k1) - Math.atan(k2);
            angle = Math.toDegrees(dk);
        }
        return angle;
    }

    public static boolean isConvex(NestPath path){
        int n = path.size();
        boolean flag = false;
        List<Segment> originSegments = path.getSegments();
        List<Segment> segments = new ArrayList<>();
        segments.add(0,null);
        for(int i = 0; i < originSegments.size(); i++){
            segments.add(originSegments.get(i));
        }
        segments.add(path.get(0));
        segments.add(path.get(1));
        for(int i = 2; i <= n+1; i++){
            if(Xji(segments.get(i-1), segments.get(i), segments.get(i+1)) > 0){
                flag = true;
            }
        }
        return !flag;
    }

    public static double Xji(Segment a, Segment b, Segment c)
    {
        double x1,y1,x2,y2;
        x1=a.x-b.x;
        y1=a.y-b.y;
        x2=c.x-b.x;
        y2=c.y-b.y;
        return x1*y2-x2*y1;
    }

    public static boolean interseccionPP(Piece pieza1, Piece pieza2)
    {
        int vertices1 = pieza1.getvertices();
        int vertices2 = pieza2.getvertices();
        boolean value;

        if ( (pieza1.getXmax() <= pieza2.getXmin())
                ||(pieza2.getXmax() <= pieza1.getXmin())
                ||(pieza1.getYmax() <= pieza2.getYmin())
                ||(pieza2.getYmax() <= pieza1.getYmin()) )
        {
            return false;
        }

        for (int i = 0; i < vertices1-1; i++)
        {  for (int j = 0; j < vertices2-1; j++)
        {
            value = interseccionSS(pieza1.coordX[i], pieza1.coordY[i],
                    pieza1.coordX[i+1], pieza1.coordY[i+1],
                    pieza2.coordX[j], pieza2.coordY[j],
                    pieza2.coordX[j+1], pieza2.coordY[j+1]);
            if ( value )
            {
                return true;
            }
        }

            value = interseccionSS(pieza1.coordX[i], pieza1.coordY[i],
                    pieza1.coordX[i+1], pieza1.coordY[i+1],
                    pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
                    pieza2.coordX[0], pieza2.coordY[0]);
            if ( value )
            {
                return true;
            }
        }

        for (int j = 0; j < vertices2-1; j++)
        {
            value = interseccionSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1],
                    pieza1.coordX[0], pieza1.coordY[0],
                    pieza2.coordX[j], pieza2.coordY[j],
                    pieza2.coordX[j+1], pieza2.coordY[j+1]);
            if ( value )
            {
                return true;
            }
        }

        value = interseccionSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1],
                pieza1.coordX[0], pieza1.coordY[0],
                pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
                pieza2.coordX[0], pieza2.coordY[0]);
        if ( value )
        {
            return true;
        }

        return false;
    }

    public static boolean interseccionSS(int X1, int Y1, int X2, int Y2, int X3, int Y3, int X4, int Y4)
    {
        double m1, m2, x, y;

        if ( (Math.max(X1, X2) <= Math.min(X3, X4))
                ||(Math.max(X3, X4) <= Math.min(X1, X2))
                ||(Math.max(Y1, Y2) <= Math.min(Y3, Y4))
                ||(Math.max(Y3, Y4) <= Math.min(Y1, Y2)) )
        {
            return false;
        }

        if (X1 == X2)
        {
            if (Y3 == Y4)
            {
                if( (X1 < Math.max(X3, X4) && X1 > Math.min(X3, X4))
                        &&(Y3 < Math.max(Y1, Y2) && Y3 > Math.min(Y1, Y2)) )
                {
                    return true;
                }
            }

            m2 = (double)(Y4-Y3)/ (double)(X4-X3);
            y = m2 * (double)(X1 - X3) + (double)(Y3);
            if( (y < Math.max(Y1, Y2) && y > Math.min(Y1, Y2))
                    && (y < Math.max(Y3, Y4) && y > Math.min(Y3, Y4)) )
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        if (X3 == X4)
        {
            if (Y1 == Y2)
            {
                if( (X3 < Math.max(X1, X2) && X3 > Math.min(X1, X2))
                        &&(Y1 < Math.max(Y3, Y4) && Y1 > Math.min(Y3, Y4)) )
                {
                    return true;
                }
            }

            m1 = (double)(Y2-Y1)/ (double)(X2-X1);
            y = m1 * (double)(X3 - X1) + (double)(Y1);
            if( (y < Math.max(Y1, Y2) && y > Math.min(Y1, Y2))
                    && (y < Math.max(Y3, Y4) && y > Math.min(Y3, Y4)) )
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        m1 = (double)(Y2-Y1)/ (double)(X2-X1);
        m2 = (double)(Y4-Y3)/ (double)(X4-X3);

        if (m1 == m2)
        {
            return false;
        }

        x = (m1*(double)X1 - (double)Y1 - m2*(double)X3 + (double)Y3) / (m1-m2);
        x = redondeaSiCerca(x);

        if( (x < Math.max(X1, X2) && x > Math.min(X1, X2))
                && (x < Math.max(X3, X4) && x > Math.min(X3, X4)) )
        {
            return true;
        }

        return false;
    }

    public static double redondeaSiCerca(double x)
    {
        double tolerancia = 0.00001;
        if( Math.abs(x - Math.ceil(x)) < tolerancia )
        {
            x = 	Math.ceil(x);
        } else if( Math.abs(x - Math.floor(x)) < tolerancia )
        {
            x = Math.floor(x);
        }

        return x;
    }

    public static boolean insidePP(NestPath pieza1, NestPath pieza2){
        Paths polygonOne = new Paths();
        polygonOne.add(scaleUp2ClipperCoordinates(pieza1));

        Paths polygonTwo = new Paths();
        polygonTwo.add(scaleUp2ClipperCoordinates(pieza2));

        Paths remain = new Paths();
        DefaultClipper clipper3 = new DefaultClipper(2);
        clipper3.addPaths(polygonTwo, Clipper.PolyType.CLIP, true);
        clipper3.addPaths(polygonOne, Clipper.PolyType.SUBJECT, true);
        clipper3.execute(Clipper.ClipType.INTERSECTION, remain, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);

        if(remain.size() == 0){
            return false;
        }

        List<NestPath> holeNFP = new ArrayList<>();
        for(int j = 0 ; j<remain.size() ; j++){
            holeNFP.add( toNestCoordinates(remain.get(j)));
        }

        if(almostEqual(abs(polygonArea(holeNFP.get(0))), abs(polygonArea(pieza1)))){
            return true;
        }

        return false;
    }

    public static boolean insidePP2(NestPath pieza1, NestPath pieza2)
    {
        boolean value;
        int vertices = pieza1.size();
        int vertices2 = pieza2.size();
        double alto;
        double ancho;

        if(  pieza1.getMaxX() <= pieza2.getMinX() ||
                pieza2.getMaxX() <= pieza1.getMinX() ||
                pieza1.getMaxY() <= pieza2.getMinY() ||
                pieza2.getMaxY() <= pieza1.getMinY() )
        {
            return false;
        }

        alto = Math.max(pieza1.getMaxX(), pieza2.getMaxX())-
                Math.min(pieza1.getMinX(), pieza2.getMinX());
        ancho = Math.max(pieza1.getMaxY(), pieza2.getMaxY())-
                Math.min(pieza1.getMinY(), pieza2.getMinY());
        if(alto * ancho < polygonArea(pieza1)  + polygonArea(pieza2))
        {
            return true;
        }

        for (int j = 0; j < vertices; j++)
        {
            value = pointInPolygon(pieza1.get(j).x, pieza1.get(j).y, pieza2);
            if(value)
            {
                return true;
            }
        }

        for (int j = 0; j < vertices-1; j++)
        {
            value = pointInPolygon((pieza1.get(j).x + pieza1.get(j+1).x)/2,
                    (pieza1.get(j).y + pieza1.get(j+1).y)/2, pieza2);
            if(value)
            {
                return true;
            }
            value = pointInPolygon((pieza1.get(j).x+pieza1.get(j+1).x)/2 +2,
                    (pieza1.get(j).y+pieza1.get(j+1).y)/2 +2, pieza1);
            if(value)
            {
                value = pointInPolygon((pieza1.get(j).x + pieza1.get(j+1).x)/2 +2,
                        (pieza1.get(j).y+pieza1.get(j+1).y)/2 + 2, pieza2);
                if(value)
                {
                    return true;
                }
            }
        }

        value = pointInPolygon((pieza1.get(vertices-1).x + pieza1.get(0).x)/2,
                (pieza1.get(vertices-1).y + pieza1.get(0).y)/2, pieza2);
        if(value)
        {
            return true;
        }
        value = pointInPolygon((pieza1.get(vertices-1).x + pieza1.get(0).x)/2 +2,
                (pieza1.get(vertices-1).y + pieza1.get(0).y)/2 +2, pieza1);
        if(value)
        {
            value = pointInPolygon((pieza1.get(vertices-1).x + pieza1.get(0).x)/2 +2,
                    (pieza1.get(vertices-1).y + pieza1.get(0).y)/2 +2, pieza2);
            if(value)
            {
                return true;
            }
        }

        value = pointInPolygon((pieza1.getMaxX()+pieza1.getMinX())/2,
                (pieza1.getMaxY() + pieza1.getMinY())/2, pieza1);
        if(value)
        {
            value = pointInPolygon((pieza1.getMaxX() + pieza1.getMinX())/2,
                    (pieza1.getMaxY() + pieza1.getMinY())/2, pieza2);
            if(value)
            {
                return true;
            }
        }
        return false;
    }

    public static boolean dentroPuntoPieza(double x1, double y1, NestPath pieza){
        return true;
    }

    public static List<NestPath> intersectPoints(List<NestPath> combineNfp, List<NestPath> binNfp) {
        List<NestPath> pointList = new ArrayList<>();

        NestPath pieza = combineNfp.get(0);
        int vertices = pieza.size();

        NestPath pieza2 = binNfp.get(0);
        int vertices2 = pieza2.size();

        for(int j = 0; j < vertices2; j++){
            double x = pieza2.get(j).x;
            double y = pieza2.get(j).y;
            boolean value;
            for (int i = 0; i < vertices-1; i++)
            {
                value = dentroPuntoSegm(x, y,
                        pieza.get(i).x, pieza.get(i).y,
                        pieza.get(i+1).x, pieza.get(i+1).y);
                if (value)
                {
                    NestPath temp = new NestPath();
                    temp.add(pieza2.get(j));
                    pointList.add(temp);
                }
            }
            value = dentroPuntoSegm(x, y,
                    pieza.get(vertices-1).x, pieza.get(vertices-1).y,
                    pieza.get(0).x, pieza.get(0).y);
            if (value)
            {
                NestPath temp = new NestPath();
                temp.add(pieza2.get(j));
                pointList.add(temp);
            }
        }
        return pointList;
    }

    private static boolean dentroPuntoSegm(double X1, double Y1, double X2, double Y2, double X3, double Y3)
    {
        if( distPuntoPunto(X1, Y1, X2, Y2)+
                distPuntoPunto(X1, Y1, X3, Y3)==
                distPuntoPunto(X2, Y2, X3, Y3) )
        {
            return true;
        }
        return false;
    }

    public static double adjancencyOP(NestPath binPolygon, NestPath path, Vector shifvector, List<NestPath> placed, List<Vector> placements){
        double adjancency = 0;

        NestPath newPath = new NestPath();
        for(int n = 0 ; n < path.size(); n++){
            newPath.add(new Segment(  path.get(n).x + shifvector.x,
                    path.get(n).y + shifvector.y));
        }
        adjancency += adjancencyPP(binPolygon, newPath);

        if(placed.isEmpty()){
            return adjancency;
        }

        for(int m = 0; m < placed.size(); m++){
            NestPath placedPath = new NestPath();
            for(int n = 0 ; n < placed.get(m).size();n++){
                placedPath.add(new Segment(  placed.get(m).get(n).x + placements.get(m).x  ,
                        placed.get(m).get(n).y +placements.get(m).y));
            }
            adjancency += adjancencyPP(placed.get(m), newPath);
        }

        return adjancency;
    }

    public static double adjancencyPP(NestPath pieza1, NestPath pieza2){
        int vertices1 = pieza1.size();
        int vertices2 = pieza2.size();
        int adyacencia = 0;

        if ( (pieza1.getMaxX() < pieza2.getMinX())
                ||(pieza2.getMaxX() < pieza1.getMinX())
                ||(pieza1.getMaxY() < pieza2.getMinY())
                ||(pieza2.getMaxY() < pieza1.getMinY()) )
        {
            return 0;
        }

        for (int i = 0; i < vertices1-1; i++)
        {  for (int j = 0; j < vertices2-1; j++)
        {
            adyacencia += adyacenciaSS(pieza1.get(i).x, pieza1.get(i).y,
                    pieza1.get(i+1).x, pieza1.get(i+1).y,
                    pieza2.get(j).x, pieza2.get(j).y,
                    pieza2.get(j+1).x, pieza2.get(j+1).y);
        }
            adyacencia += adyacenciaSS(pieza1.get(i).x, pieza1.get(i).y,
                    pieza1.get(i+1).x, pieza1.get(i+1).y,
                    pieza2.get(vertices2-1).x, pieza2.get(vertices2-1).y,
                    pieza2.get(0).x, pieza2.get(0).y);
        }

        for (int j = 0; j < vertices2-1; j++)
        {
            adyacencia += adyacenciaSS(pieza1.get(vertices1-1).x, pieza1.get(vertices1-1).y,
                    pieza1.get(0).x, pieza1.get(0).y,
                    pieza2.get(j).x, pieza2.get(j).y,
                    pieza2.get(j+1).x, pieza2.get(j+1).y);
        }

        adyacencia += adyacenciaSS(pieza1.get(vertices1-1).x, pieza1.get(vertices1-1).y,
                pieza1.get(0).x, pieza1.get(0).y,
                pieza2.get(vertices2-1).x, pieza2.get(vertices2-1).y,
                pieza2.get(0).x, pieza2.get(0).y);
        return adyacencia;

    }

    public static double adyacenciaSS(double X1, double Y1, double X2, double Y2, double X3, double Y3, double X4, double Y4)
    {
        double adyacencia = 0;
        double m1, m2, b1, b2;

        if ( (max(X1, X2) < min(X3, X4))
                ||(max(X3, X4) < min(X1, X2))
                ||(max(Y1, Y2) < min(Y3, Y4))
                ||(max(Y3, Y4) < min(Y1, Y2)) )
        {
            return 0;
        }

        if (X1 == X2 && X3 == X4)
        {
            if(   (Y1 <= Math.max(Y3,Y4)) && (Y1 >= Math.min(Y3,Y4))
                    &&  (Y2 <= Math.max(Y3,Y4)) && (Y2 >= Math.min(Y3,Y4)) )
            {
                return Math.abs(Y2-Y1);
            }
            if(   (Y3 <= Math.max(Y1,Y2)) && (Y3 >= Math.min(Y1,Y2))
                    &&  (Y4 <= Math.max(Y1,Y2)) && (Y4 >= Math.min(Y1,Y2)) )
            {
                return Math.abs(Y4-Y3);
            }
            if(  Math.max(Y1,Y2) > Math.max(Y3,Y4) )
            {
                adyacencia = Math.max(Y3,Y4) - Math.min(Y1,Y2);
                return adyacencia;
            }
            if(  Math.max(Y3,Y4) > Math.max(Y1,Y2) )
            {
                adyacencia = Math.max(Y1,Y2) - Math.min(Y3,Y4);
                return adyacencia;
            }
        }

        if (X1 == X2 || X3 == X4)
        {
            return 0;
        }

        if (Y1 == Y2 && Y3 == Y4)
        {
            if(   (X1 <= Math.max(X3,X4)) && (X1 >= Math.min(X3,X4))
                    &&  (X2 <= Math.max(X3,X4)) && (X2 >= Math.min(X3,X4)) )
            {
                return Math.abs(X2-X1);
            }
            if(   (X3 <= Math.max(X1,X2)) && (X3 >= Math.min(X1,X2))
                    &&  (X4 <= Math.max(X1,X2)) && (X4 >= Math.min(X1,X2)) )
            {
                return Math.abs(X4-X3);
            }
            if(  Math.max(X1,X2) > Math.max(X3,X4) )
            {
                adyacencia = Math.max(X3,X4) - Math.min(X1,X2);
                return adyacencia;
            }
            if(  Math.max(X3,X4) > Math.max(X1,X2) )
            {
                adyacencia = Math.max(X1,X2) - Math.min(X3,X4);
                return adyacencia;
            }
        }

        m1 = (double)(Y2-Y1)/ (double)(X2-X1);
        m2 = (double)(Y4-Y3)/ (double)(X4-X3);
        if (m1 != m2)
        {
            return 0;
        }

        b1 = (double)(Y1) - m1*(double)(X1);
        b2 = (double)(Y3) - m2*(double)(X3);
        if (b1 != b2)
        {
            return 0;
        }

        if(   (Y1 <= Math.max(Y3,Y4)) && (Y1 >= Math.min(Y3,Y4))
                &&  (Y2 <= Math.max(Y3,Y4)) && (Y2 >= Math.min(Y3,Y4)) )
        {
            adyacencia = distPuntoPunto(X1, Y1, X2, Y2);
            return adyacencia;
        }
        if(   (Y3 <= Math.max(Y1,Y2)) && (Y3 >= Math.min(Y1,Y2))
                &&  (Y4 <= Math.max(Y1,Y2)) && (Y4 >= Math.min(Y1,Y2)) )
        {
            adyacencia = distPuntoPunto(X3, Y3, X4, Y4);
            return adyacencia;
        }
        if(  Math.max(Y1,Y2) > Math.max(Y3,Y4) )
        {
            if(m1 > 0)
            {
                adyacencia = distPuntoPunto(max(X3,X4), max(Y3,Y4), min(X1,X2), min(Y1,Y2));
                return adyacencia;
            }
            adyacencia = distPuntoPunto(min(X3,X4), max(Y3,Y4), max(X1,X2), min(Y1,Y2));
            return adyacencia;
        }
        if(  Math.max(Y3,Y4) > Math.max(Y1,Y2) )
        {
            if(m1 > 0)
            {
                adyacencia = distPuntoPunto(max(X1,X2), max(Y1,Y2), min(X3,X4), min(Y3,Y4));
                return adyacencia;
            }
            adyacencia = distPuntoPunto(min(X1,X2), max(Y1,Y2), max(X3,X4), min(Y3,Y4));
            return adyacencia;
        }
        return adyacencia;
    }

    private static double distPuntoPunto(double X1, double Y1, double X2, double Y2)
    {
        return sqrt(pow(X2-X1, 2)+pow(Y2-Y1, 2));
    }

    public static double adjancencyAllEdgeRatioPP(NestPath pieza1, NestPath pieza2){
        int vertices1 = pieza1.size();
        int vertices2 = pieza2.size();
        double ratio = 0;
        double len1;
        double len2;
        double edgeOverlap;

        if ( (pieza1.getMaxX() < pieza2.getMinX())
                ||(pieza2.getMaxX() < pieza1.getMinX())
                ||(pieza1.getMaxY() < pieza2.getMinY())
                ||(pieza2.getMaxY() < pieza1.getMinY()) )
        {
            return 0;
        }

        for (int i = 0; i < vertices1-1; i++)
        {
            len1 = lenofLineSegment(pieza1.get(i).x, pieza1.get(i).y, pieza1.get(i+1).x, pieza1.get(i+1).y);
            for (int j = 0; j < vertices2-1; j++)
                {
                    len2 = lenofLineSegment(pieza2.get(j).x, pieza2.get(j).y, pieza2.get(j+1).x, pieza2.get(j+1).y);
                    edgeOverlap = adyacenciaSS(pieza1.get(i).x, pieza1.get(i).y,
                            pieza1.get(i+1).x, pieza1.get(i+1).y,
                            pieza2.get(j).x, pieza2.get(j).y,
                            pieza2.get(j+1).x, pieza2.get(j+1).y);
                    ratio += Math.pow(edgeOverlap / (len1 > len2 ? len1:len2),2);
                }
                len2 = lenofLineSegment(pieza2.get(vertices2-1).x, pieza2.get(vertices2-1).y, pieza2.get(0).x, pieza2.get(0).y);
                edgeOverlap = adyacenciaSS(pieza1.get(i).x, pieza1.get(i).y,
                        pieza1.get(i+1).x, pieza1.get(i+1).y,
                        pieza2.get(vertices2-1).x, pieza2.get(vertices2-1).y,
                        pieza2.get(0).x, pieza2.get(0).y);
                ratio += Math.pow(edgeOverlap / (len1 > len2 ? len1:len2),2);
        }

        for (int j = 0; j < vertices2-1; j++)
        {
            len1 = lenofLineSegment(pieza1.get(vertices1-1).x, pieza1.get(vertices1-1).y, pieza1.get(0).x, pieza1.get(0).y);
            len2 = lenofLineSegment(pieza2.get(j).x, pieza2.get(j).y, pieza2.get(j+1).x, pieza2.get(j+1).y);
            edgeOverlap = adyacenciaSS(pieza1.get(vertices1-1).x, pieza1.get(vertices1-1).y,
                    pieza1.get(0).x, pieza1.get(0).y,
                    pieza2.get(j).x, pieza2.get(j).y,
                    pieza2.get(j+1).x, pieza2.get(j+1).y);
            ratio += Math.pow(edgeOverlap / (len1 > len2 ? len1:len2),2);
        }

        len1 = lenofLineSegment(pieza1.get(vertices1-1).x, pieza1.get(vertices1-1).y, pieza1.get(0).x, pieza1.get(0).y);
        len2 = lenofLineSegment(pieza2.get(vertices2-1).x, pieza2.get(vertices2-1).y, pieza2.get(0).x, pieza2.get(0).y);
        edgeOverlap = adyacenciaSS(pieza1.get(vertices1-1).x, pieza1.get(vertices1-1).y,
                pieza1.get(0).x, pieza1.get(0).y,
                pieza2.get(vertices2-1).x, pieza2.get(vertices2-1).y,
                pieza2.get(0).x, pieza2.get(0).y);
        ratio += Math.pow(edgeOverlap / (len1 > len2 ? len1:len2),2);
        return ratio;
    }

    public static double lenofLineSegment(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1-y2),2));
    }

    public static double polygonArea(NestPath polygon){
        double area = 0;
        for(int i = 0  , j = polygon.size()-1; i < polygon.size() ; j = i++){
            Segment si = polygon.getSegments().get(i);
            Segment sj = polygon.getSegments().get(j);
            area += ( sj.getX() +si.getX()) * (sj.getY() - si.getY());
        }
        return Math.abs(0.5*area);
    }

    public static boolean onSegment(Segment A, Segment B , Segment p ){
        if(almostEqual(A.x, B.x) && almostEqual(p.x, A.x)){
            if(!almostEqual(p.y, B.y) && !almostEqual(p.y, A.y) && p.y < Math.max(B.y, A.y) && p.y > Math.min(B.y, A.y)){
                return true;
            }
            else{
                return false;
            }
        }

        if(almostEqual(A.y, B.y) && almostEqual(p.y, A.y)){
            if(!almostEqual(p.x, B.x) && !almostEqual(p.x, A.x) && p.x < Math.max(B.x, A.x) && p.x > Math.min(B.x, A.x)){
                return true;
            }
            else{
                return false;
            }
        }

        if((p.x < A.x && p.x < B.x) || (p.x > A.x && p.x > B.x) || (p.y < A.y && p.y < B.y) || (p.y > A.y && p.y > B.y)){
            return false;
        }

        if((almostEqual(p.x, A.x) && almostEqual(p.y, A.y)) || (almostEqual(p.x, B.x) && almostEqual(p.y, B.y))){
            return false;
        }

        double cross = (p.y - A.y) * (B.x - A.x) - (p.x - A.x) * (B.y - A.y);

        if(Math.abs(cross) > TOL){
            return false;
        }

        double dot = (p.x - A.x) * (B.x - A.x) + (p.y - A.y)*(B.y - A.y);

        if(dot < 0 || almostEqual(dot, 0)){
            return false;
        }

        double len2 = (B.x - A.x)*(B.x - A.x) + (B.y - A.y)*(B.y - A.y);

        if(dot > len2 || almostEqual(dot, len2)){
            return false;
        }

        return true;

    }

    public static Boolean pointInPolygon(Segment point ,NestPath polygon){
        boolean inside = false;
        double offsetx = polygon.offsetX;
        double offsety = polygon.offsetY;

        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j=i++) {
            double xi = polygon.get(i).x + offsetx;
            double yi = polygon.get(i).y + offsety;
            double xj = polygon.get(j).x + offsetx;
            double yj = polygon.get(j).y + offsety;

            if(almostEqual(xi, point.x) && almostEqual(yi, point.y)){
                return false;
            }

            if(onSegment( new Segment(xi,yi),new Segment(xj,yj) , point)){
                return false;
            }

            if(almostEqual(xi, xj) && almostEqual(yi, yj)){
                continue;
            }

            boolean intersect = ((yi > point.y) != (yj > point.y)) && (point.x < (xj - xi) * (point.y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }

        return inside;
    }

    public static Boolean pointInPolygon(double x, double y,NestPath polygon){
        boolean inside = false;
        double offsetx = polygon.offsetX;
        double offsety = polygon.offsetY;

        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j=i++) {
            double xi = polygon.get(i).x + offsetx;
            double yi = polygon.get(i).y + offsety;
            double xj = polygon.get(j).x + offsetx;
            double yj = polygon.get(j).y + offsety;

            if(almostEqual(xi, x) && almostEqual(yi, y)){
                return false;
            }

            if(onSegment( new Segment(xi,yi),new Segment(xj,yj) , new Segment(x,y))){
                return false;
            }

            if(almostEqual(xi, xj) && almostEqual(yi, yj)){
                continue;
            }

            boolean intersect = ((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }

        return inside;
    }

    public static Bound getPolygonBounds(NestPath polygon){

        double xmin = polygon.getSegments().get(0).getX();
        double xmax = polygon.getSegments().get(0).getX();
        double ymin = polygon.getSegments().get(0).getY();
        double ymax = polygon.getSegments().get(0).getY();

        for(int i = 1 ; i <polygon.getSegments().size(); i ++){
            double x = polygon.getSegments().get(i).getX();
            double y = polygon.getSegments().get(i).getY();
            if(x > xmax ){
                xmax = x;
            }
            else if(x < xmin){
                xmin = x;
            }

            if(y > ymax ){
                ymax =y;
            }
            else if(y< ymin ){
                ymin = y;
            }
        }
        return new Bound(xmin,ymin,xmax-xmin , ymax-ymin);
    }

    public static NestPath rotatePolygon2PolygonOrigin(NestPath polygon , int degrees ){
        NestPath rotated = new NestPath();
        double angle = degrees * Math.PI / 180;
        for(int i = 0 ; i< polygon.size() ; i++){
            double x = polygon.get(i).x;
            double y = polygon.get(i).y;
            double x1 = x*Math.cos(angle)-y*Math.sin(angle);
            double y1 = x*Math.sin(angle)+y*Math.cos(angle);
            rotated.add(new Segment(x1 , y1));
        }
        rotated.bid = polygon.bid;
        rotated.setId(polygon.getId());
        rotated.setSource(polygon.getSource());
        rotated.setArea(polygon.getArea());
        if(polygon.getChildren().size() > 0 ){
            for(int j = 0 ; j<polygon.getChildren().size() ; j ++){
                rotated.getChildren().add( rotatePolygon2Polygon(polygon.getChildren().get(j) , degrees));
            }
        }
        return rotated;
    }

    public static NestPath rotatePolygon2Polygon(NestPath polygon , int degrees ){
        NestPath rotated = new NestPath();
        double angle = Math.toRadians((double)degrees);
        for(int i = 0 ; i< polygon.size() ; i++){
            double x = polygon.get(i).x;
            double y = polygon.get(i).y;
            double x1 = x*Math.cos(angle)-y*Math.sin(angle);
            double y1 = x*Math.sin(angle)+y*Math.cos(angle);
            rotated.add(new Segment(x1 , y1));
        }
        rotated.bid = polygon.bid;
        rotated.setId(polygon.getId());
        rotated.setSource(polygon.getSource());
        rotated.setArea(polygon.getArea());
        if(polygon.getChildren().size() > 0 ){
            for(int j = 0 ; j<polygon.getChildren().size() ; j ++){
                rotated.getChildren().add( rotatePolygon2Polygon(polygon.getChildren().get(j) , degrees));
            }
        }

        return rotated;
    }

    public static boolean isRectangle(NestPath poly , double tolerance){
        Bound bb = getPolygonBounds(poly);

        for(int i = 0 ; i< poly.size();i++){
            if( !almostEqual(poly.get(i).x , bb.getXmin(),tolerance) && ! almostEqual(poly.get(i).x , bb.getXmin() + bb.getWidth(), tolerance)){
                return false;
            }
            if( ! almostEqual(poly.get(i).y , bb.getYmin() ,tolerance) && ! almostEqual(poly.get(i).y , bb.getYmin() + bb.getHeight() ,tolerance)){
                return false;
            }
        }
        return true;
    }

    public static List<NestPath> noFitPolygon(final NestPath A ,final  NestPath B , boolean inside , boolean searchEdges){
        A.setOffsetX(0);
        A.setOffsetY(0);

        double minA = A.get(0).y;
        int minAIndex = 0;
        double currentAX = A.get(0).x;
        double maxB = B.get(0).y;
        int maxBIndex = 0;

        for(int i = 1 ; i< A.size(); i ++){
            A.get(i).marked = false;
            if(almostEqual(A.get(i).y , minA ) && A.get(i).x < currentAX ){
                minA = A.get(i).y;
                minAIndex = i;
                currentAX = A.get(i).x;
            }
            else if(A.get(i).y < minA ){
                minA = A.get(i).y;
                minAIndex = i;
                currentAX = A.get(i).x;
            }
        }
        for(int i  =1 ; i<B.size() ; i ++){
            B.get(i).marked = false;
            if(B.get(i).y >maxB ){
                maxB = B.get(i).y;
                maxBIndex = i;
            }
        }
        Segment startPoint = null ;
        if(!inside){
            startPoint = new Segment(A.get(minAIndex).x - B.get(maxBIndex).x ,
                                     A.get(minAIndex).y - B.get(maxBIndex).y);

        }
        else{
            startPoint = searchStartPoint(A,B, true , null);

        }

        List<NestPath> NFPlist = new ArrayList<NestPath>();

        while(startPoint != null ){
            Segment prevvector = null;
            B.setOffsetX(startPoint.x);
            B.setOffsetY(startPoint.y);


            List<SegmentRelation> touching;
            NestPath NFP = new NestPath();
            NFP.add(new Segment(B.get(0).x + B.getOffsetX(),
                                B.get(0).y + B.getOffsetY()));

            double referenceX = B.get(0).x + B.getOffsetX();
            double referenceY = B.get(0).y + B.getOffsetY();
            double startX = referenceX;
            double startY = referenceY;
            int counter = 0 ;

            while( counter < 10 *( A.size() + B.size())){
                touching = new ArrayList<SegmentRelation>();


                for(int i = 0 ; i <A.size();i++){
                    int nexti = (i == A.size()-1) ? 0 : i +1;
                    for(int j = 0 ; j < B.size() ; j++){
                        int nextj = (j == B.size()-1 ) ? 0: j+1;
                        if(almostEqual(A.get(i).x, B.get(j).x+B.offsetX) && almostEqual(A.get(i).y, B.get(j).y+B.offsetY)){
                            touching.add(new SegmentRelation(0,i,j));
                        }
                        else if(onSegment(A.get(i),A.get(nexti),new Segment(B.get(j).x+B.offsetX, B.get(j).y + B.offsetY))){
                            touching.add(new SegmentRelation(1, nexti,j));
                        }
                        else if(onSegment( new Segment(B.get(j).x +B.offsetX , B.get(j).y +B.offsetY),
                                           new Segment(B.get(nextj).x+B.offsetX , B.get(nextj).y + B.offsetY),
                                            A.get(i))){
                            touching.add( new SegmentRelation(2 , i , nextj));
                        }
                    }
                }


                NestPath vectors = new NestPath();
                for(int i = 0; i < touching.size() ; i++){
                    Segment vertexA = A.get(touching.get(i).A);
                    vertexA.marked = true;

                    int prevAIndex = touching.get(i).A -1;
                    int nextAIndex = touching.get(i).A +1;

                    prevAIndex = (prevAIndex < 0) ? A.size()-1 : prevAIndex;
                    nextAIndex = (nextAIndex >= A.size()) ? 0 : nextAIndex;

                    Segment prevA = A.get(prevAIndex);
                    Segment nextA = A.get(nextAIndex);

                    Segment vertexB = B.get(touching.get(i).B);

                    int prevBIndex = touching.get(i).B -1;
                    int nextBIndex = touching.get(i).B +1;

                    prevBIndex = (prevBIndex < 0) ? B.size()-1 : prevBIndex;
                    nextBIndex = (nextBIndex >= B.size()) ? 0 : nextBIndex;

                    Segment prevB = B.get(prevBIndex);
                    Segment nextB = B.get(nextBIndex);

                    if(touching.get(i).type == 0 ){
                        Segment vA1 = new Segment(prevA.x - vertexA.x , prevA.y - vertexA.y);
                        vA1.start = vertexA ; vA1.end = prevA;

                        Segment vA2 = new Segment(nextA.x - vertexA.x , nextA.y - vertexA.y);
                        vA2.start = vertexA; vA2.end = nextA;

                        Segment vB1 = new Segment(vertexB.x - prevB.x , vertexB.y - prevB.y );
                        vB1.start = prevB; vB1.end = vertexB;

                        Segment vB2 = new Segment(vertexB.x - nextB.x , vertexB.y - nextB.y);
                        vB2.start = nextB ; vB2.end = vertexB;

                        vectors.add(vA1);
                        vectors.add(vA2);
                        vectors.add(vB1);
                        vectors.add(vB2);
                    }
                    else if (touching.get(i).type ==1 ){

                        Segment tmp = new Segment( vertexA.x - (vertexB.x +B.offsetX) ,
                                                    vertexA.y - (vertexB.y +B.offsetY));

                        tmp.start = prevA;
                        tmp.end = vertexA;

                        Segment tmp2 = new Segment(prevA.x-(vertexB.x+B.offsetX) ,prevA.y-(vertexB.y+B.offsetY) );
                        tmp2.start = vertexA ; tmp2.end = prevA;
                        vectors.add(tmp);
                        vectors.add(tmp2);

                    }
                    else if (touching.get(i).type == 2 ){
                        Segment tmp1 = new Segment( vertexA.x - (vertexB.x + B.offsetX) ,
                                                    vertexA.y - (vertexB.y + B.offsetY));
                        tmp1.start = prevB;
                        tmp1.end = vertexB;
                        Segment tmp2 = new Segment(vertexA.x - (prevB.x +B.offsetX),
                                                   vertexA.y - (prevB.y + B.offsetY));
                        tmp2.start = vertexB;
                        tmp2.end = prevB;

                        vectors.add(tmp1); vectors.add(tmp2);
                    }
                }

                Segment translate = null;

                Double maxd = 0.0;
                for(int i = 0 ; i <vectors.size() ; i ++){
                    if(almostEqual(vectors.get(i).x , 0 ) && almostEqual(vectors.get(i).y , 0 ) ){
                        continue;
                    }

                    if(prevvector != null  &&  vectors.get(i).y * prevvector.y + vectors.get(i).x * prevvector.x < 0 ){

                        double vectorlength = Math.sqrt(vectors.get(i).x*vectors.get(i).x+vectors.get(i).y*vectors.get(i).y);
                        Segment unitv = new Segment(vectors.get(i).x/vectorlength , vectors.get(i).y/vectorlength);


                        double prevlength = Math.sqrt(prevvector.x*prevvector.x+prevvector.y*prevvector.y);
                        Segment prevunit = new Segment(prevvector.x / prevlength , prevvector.y / prevlength);


                        if(Math.abs(unitv.y * prevunit.x - unitv.x * prevunit.y) < 0.0001){

                            continue;
                        }
                    }
                    Double d = polygonSlideDistance(A,B, vectors.get(i) , true);

                    double vecd2 = vectors.get(i).x*vectors.get(i).x + vectors.get(i).y*vectors.get(i).y;

                    if(d == null || d*d > vecd2){
                        double vecd = Math.sqrt(vectors.get(i).x*vectors.get(i).x + vectors.get(i).y*vectors.get(i).y);
                        d = vecd;
                    }

                    if(d != null && d > maxd){
                        maxd = d;
                        translate = vectors.get(i);
                    }

                }

                if(translate == null || almostEqual(maxd, 0)){
                    if(translate == null ){

                    }
                    if( almostEqual(maxd ,0 )){
                    }
                    NFP = null;
                    break;
                }

                translate.start.marked = true;
                translate.end.marked = true;

                prevvector = translate;


                double vlength2 = translate.x*translate.x + translate.y*translate.y;
                if(maxd*maxd < vlength2 && !almostEqual(maxd*maxd, vlength2)){
                    double scale = Math.sqrt((maxd*maxd)/vlength2);
                    translate.x *= scale;
                    translate.y *= scale;
                }

                referenceX += translate.x;
                referenceY += translate.y;


                if(almostEqual(referenceX, startX) && almostEqual(referenceY, startY)){
                    break;
                }

                boolean looped = false;
                if(NFP.size() > 0){
                    for(int i=0; i<NFP.size()-1; i++){
                        if(almostEqual(referenceX, NFP.get(i).x) && almostEqual(referenceY, NFP.get(i).y)){
                            looped = true;
                        }
                    }
                }

                if(looped){
                    break;
                }

                NFP.add(new Segment(referenceX,referenceY));

                B.offsetX += translate.x;
                B.offsetY += translate.y;
                counter++;
            }

            if(NFP != null && NFP.size() > 0){
                NFPlist.add(NFP);
            }

            if(!searchEdges){
                break;
            }
            startPoint  = searchStartPoint(A,B,inside,NFPlist);
        }
        return NFPlist;
    }

    public static Segment searchStartPoint(NestPath CA ,NestPath CB , boolean inside ,List<NestPath> NFP){

        NestPath A = new NestPath(CA);
        NestPath B = new NestPath(CB);

        if(A.get(0) != A.get(A.size()-1)){
            A.add(A.get(0));
        }

        if(B.get(0) != B.get(B.size()-1)){
            B.add(B.get(0));
        }

        for(int i=0; i<A.size()-1; i++){
            if(!A.get(i).marked){
                A.get(i).marked = true;
                for(int j=0; j<B.size(); j++){
                    B.offsetX = A.get(i).x - B.get(j).x;
                    B.offsetY = A.get(i).y - B.get(j).y;
                    Boolean Binside = null;
                    for(int k=0; k<B.size(); k++){
                        Boolean inpoly = pointInPolygon( new Segment(B.get(k).x +B.offsetX , B.get(k).y +B.offsetY), A);
                        if(inpoly != null){
                            Binside = inpoly;
                            break;
                        }
                    }

                    if(Binside == null){
                        return null;
                    }

                    Segment startPoint = new Segment(B.offsetX , B.offsetY);

                    if(((Binside != null  && inside) || (Binside == null && !inside)) && !intersect(A,B) && !inNfp(startPoint, NFP)){
                        return startPoint;
                    }

                    double vx = A.get(i+1).x - A.get(i).x;
                    double vy = A.get(i+1).y - A.get(i).y;

                    Double d1 = polygonProjectionDistance(A,B, new Segment(vx , vy));
                    Double d2 = polygonProjectionDistance(B,A, new Segment(-vx,-vy));

                    Double d = null;

                    if(d1 == null && d2 == null){
                    }
                    else if(d1 == null){
                        d = d2;
                    }
                    else if(d2 == null){
                        d = d1;
                    }
                    else{
                        d = Math.min(d1,d2);
                    }

                    if(d != null && !almostEqual(d,0) && d > 0){

                    }
                    else{
                        continue;
                    }

                    double vd2 = vx*vx + vy*vy;

                    if(d*d < vd2 && !almostEqual(d*d, vd2)){
                        double vd = Math.sqrt(vx*vx + vy*vy);
                        vx *= d/vd;
                        vy *= d/vd;
                    }

                    B.offsetX += vx;
                    B.offsetY += vy;

                    for(int k=0; k<B.size(); k++){
                        Boolean inpoly = pointInPolygon(new Segment(B.get(k).x +B.offsetX , B.get(k).y +B.offsetY), A);
                        if(inpoly != null){
                            Binside = inpoly;
                            break;
                        }
                    }
                    startPoint = new Segment(B.offsetX,B.offsetY);
                    if(((Binside && inside) || (!Binside && !inside)) && !intersect(A,B) && !inNfp(startPoint, NFP)){
                        return startPoint;
                    }
                }
            }
        }
        return null;
    }

    public static boolean inNfp(Segment p , List<NestPath> nfp){
        if(nfp == null ){
            return false;
        }
        for(int i = 0 ;i <nfp.size();i++){
            for(int j = 0 ; j <nfp.get(i).size();j++){
                if(almostEqual(p.x , nfp.get(i).get(j).x ) && almostEqual(p.y , nfp.get(i).get(j).y )){
                    return true;
                }
            }
        }
        return false;
    }

    public static Double polygonProjectionDistance(NestPath CA , NestPath CB , Segment direction){
        double Boffsetx = CB.offsetX ;
        double Boffsety = CB.offsetY ;

        double Aoffsetx = CA.offsetX;
        double Aoffsety = CA.offsetY;

        NestPath A = new NestPath(CA);
        NestPath B = new NestPath(CB);

        if(A.get(0) != A.get(A.size()-1)){
            A.add(A.get(0));
        }

        if(B.get(0)!= B.get(B.size()-1)){
            B.add(B.get(0));
        }

        NestPath edgeA = A;
        NestPath edgeB = B;

        Double distance = null;
        Segment p,s1,s2 = null;
        Double d = null;
        for(int i=0; i<edgeB.size(); i++){
            Double minprojection = null;
            Segment minp = null;
            for(int j=0; j<edgeA.size()-1; j++){
                p = new Segment(edgeB.get(i).x + Boffsetx , edgeB.get(i).y+Boffsety);
                s1 = new Segment(edgeA.get(j).x +Aoffsetx ,edgeA.get(j).y +Aoffsety);
                s2 = new Segment(edgeA.get(j+1).x +Aoffsetx , edgeA.get(j+1).y +Aoffsety);
                if(Math.abs((s2.y-s1.y) * direction.x - (s2.x-s1.x) * direction.y) < TOL){
                    continue;
                }

                d = pointDistance(p, s1, s2, direction , null);

                if(d != null && (minprojection == null || d < minprojection)){
                    minprojection = d;
                    minp = p;
                }
            }
            if(minprojection != null && (distance == null || minprojection > distance)){
                distance = minprojection;
            }
        }
        return distance;
    }

    public static boolean intersect(final NestPath CA,final NestPath CB){
        double Aoffsetx = CA.offsetX ;
        double Aoffsety = CA.offsetY ;

        double Boffsetx = CB.offsetX ;
        double Boffsety = CB.offsetY ;

        NestPath A = new NestPath(CA);
        NestPath B = new NestPath(CB);

        for(int i=0; i<A.size()-1; i++){
            for(int j=0; j<B.size()-1; j++){
                Segment a1 = new Segment( A.get(i).x+Aoffsetx ,A.get(i).y+Aoffsety);
                Segment a2 = new Segment(A.get(i+1).x +Aoffsetx , A.get(i+1).y +Aoffsety);
                Segment b1 = new Segment(B.get(j).x + Boffsetx , B.get(j).y +Boffsety);
                Segment b2 = new Segment(B.get(j+1).x+Boffsetx , B.get(j+1).y+Boffsety);


                int prevbindex = (j == 0) ? B.size()-1 : j-1;
                int prevaindex = (i == 0) ? A.size()-1 : i-1;
                int nextbindex = (j+1 == B.size()-1) ? 0 : j+2;
                int nextaindex = (i+1 == A.size()-1) ? 0 : i+2;

                if(B.get(prevbindex) == B.get(j) || (almostEqual(B.get(prevbindex).x, B.get(j).x) && almostEqual(B.get(prevbindex).y, B.get(j).y))){
                    prevbindex = (prevbindex == 0) ? B.size()-1 : prevbindex-1;
                }

                if(A.get(prevaindex) == A.get(i) || (almostEqual(A.get(prevaindex).x, A.get(i).x) && almostEqual(A.get(prevaindex).y, A.get(i).y))){
                    prevaindex = (prevaindex == 0) ? A.size()-1 : prevaindex-1;
                }

                if(B.get(nextbindex) == B.get(j+1) || (almostEqual(B.get(nextbindex).x, B.get(j+1).x) && almostEqual(B.get(nextbindex).y, B.get(j+1).y))){
                    nextbindex = (nextbindex == B.size()-1) ? 0 : nextbindex+1;
                }

                if(A.get(nextaindex) == A.get(i+1) || (almostEqual(A.get(nextaindex).x, A.get(i+1).x) && almostEqual(A.get(nextaindex).y, A.get(i+1).y))){
                    nextaindex = (nextaindex == A.size()-1) ? 0 : nextaindex+1;
                }

                Segment a0 = new Segment(A.get(prevaindex).x +Aoffsetx , A.get(prevaindex).y +Aoffsety);
                Segment b0 = new Segment(B.get(prevbindex).x +Boffsetx ,B.get(prevbindex).y +Boffsety);

                Segment a3 = new Segment(A.get(nextaindex).x + Aoffsetx , A.get(nextaindex).y +Aoffsety);
                Segment b3 = new Segment(B.get(nextbindex).x +Boffsetx , B.get(nextbindex).y +Boffsety);

                if(onSegment(a1,a2,b1) || (almostEqual(a1.x, b1.x , 0.01) && almostEqual(a1.y, b1.y,0.01))){
                    Boolean b0in = pointInPolygon(b0, A);
                    Boolean b2in = pointInPolygon(b2, A);
                    if(b0in == null || b2in == null  ){
                        continue;
                    }
                    if((b0in == true && b2in == false) || (b0in == false && b2in == true)){

                        return true;
                    }
                    else{
                        continue;
                    }
                }

                if(onSegment(a1,a2,b2) || (almostEqual(a2.x, b2.x) && almostEqual(a2.y, b2.y))){
                    Boolean b1in = pointInPolygon(b1, A);
                    Boolean b3in = pointInPolygon(b3, A);
                    if(b1in == null || b3in == null){
                        continue;
                    }
                    if((b1in == true && b3in == false) || (b1in == false && b3in == true)){

                        return true;
                    }
                    else{
                        continue;
                    }
                }

                if(onSegment(b1,b2,a1) || (almostEqual(a1.x, b2.x) && almostEqual(a1.y, b2.y))){
                    Boolean a0in = pointInPolygon(a0, B);
                    Boolean a2in = pointInPolygon(a2, B);
                    if(a0in == null || a2in == null ){
                        continue;
                    }
                    if((a0in == true && a2in == false) || (a0in == false && a2in == true)){

                        return true;
                    }
                    else{
                        continue;
                    }
                }

                if(onSegment(b1,b2,a2) || (almostEqual(a2.x, b1.x) && almostEqual(a2.y, b1.y))){
                    Boolean a1in = pointInPolygon(a1, B);
                    Boolean a3in = pointInPolygon(a3, B);
                    if(a1in == null || a3in == null ){
                        continue;
                    }

                    if((a1in == true && a3in == false) || (a1in == false && a3in == true)){

                        return true;
                    }
                    else{
                        continue;
                    }
                }

                Segment p = lineIntersect(b1, b2, a1, a2 ,null);

                if(p != null){

                    return true;
                }
            }
        }

        return false;
    }

    public static Segment lineIntersect(Segment A ,Segment B ,Segment E ,Segment F , Boolean infinite){
        double a1, a2, b1, b2, c1, c2, x, y;

        a1= B.y-A.y;
        b1= A.x-B.x;
        c1= B.x*A.y - A.x*B.y;
        a2= F.y-E.y;
        b2= E.x-F.x;
        c2= F.x*E.y - E.x*F.y;

        double denom=a1*b2 - a2*b1;

        x = (b1*c2 - b2*c1)/denom;
        y = (a2*c1 - a1*c2)/denom;

        if( !Double.isFinite(x) || !Double.isFinite(y)){
            return null;
        }

        if(infinite== null || !infinite){
            if (Math.abs(A.x-B.x) > TOL && (( A.x < B.x ) ? x < A.x || x > B.x : x > A.x || x < B.x )) return null;
            if (Math.abs(A.y-B.y) > TOL && (( A.y < B.y ) ? y < A.y || y > B.y : y > A.y || y < B.y )) return null;

            if (Math.abs(E.x-F.x) > TOL && (( E.x < F.x ) ? x < E.x || x > F.x : x > E.x || x < F.x )) return null;
            if (Math.abs(E.y-F.y) > TOL && (( E.y < F.y ) ? y < E.y || y > F.y : y > E.y || y < F.y )) return null;
        }
        return new Segment(x,y);
    }

    public static Double polygonSlideDistance(final NestPath TA ,final NestPath TB , Segment direction , boolean ignoreNegative ){
        double Aoffsetx = TA.offsetX;
        double Aoffsety = TA.offsetY;

        double Boffsetx = TB.offsetX;
        double BoffsetY = TB.offsetY;

        NestPath A = new NestPath(TA);
        NestPath B = new NestPath(TB);

        if(A.get(0 ) != A.get(A.size()-1)){
            A.add(A.get(0));
        }
        if(B.get(0) != B.get(B.size() -1 )){
            B.add(B.get(0));
        }

        NestPath edgeA = A;
        NestPath edgeB = B;

        Double distance = null;


        Segment dir = normalizeVector(direction);

        Segment normal = new Segment(dir.y , -dir.x);

        Segment reverse = new Segment(-dir.x , -dir.y );

        Segment A1,A2 ,B1,B2 = null;
        for(int i = 0; i <edgeB.size() - 1 ; i++){
            for(int j = 0 ; j< edgeA.size() -1 ; j ++){
                A1 = new Segment(edgeA.get(j).x + Aoffsetx , edgeA.get(j).y +Aoffsety);
                A2 = new Segment(edgeA.get(j+1) .x +Aoffsetx , edgeA.get(j+1).y +Aoffsety );
                B1 = new Segment(edgeB.get(i).x + Boffsetx , edgeB.get(i).y +BoffsetY );
                B2 = new Segment(edgeB.get(i+1).x +Boffsetx , edgeB.get(i+1).y +BoffsetY);

                if( (almostEqual(A1.x ,A2.x ) && almostEqual(A1.y , A2.y )) || (almostEqual(B1.x,B2.x ) &&almostEqual(B1.y ,B2.y))){
                    continue;
                }
                Double d = segmentDistance(A1,A2,B1,B2 ,dir);

                if(d != null && (distance == null || d < distance)){
                    if(!ignoreNegative || d > 0 || almostEqual(d, 0)){
                        distance = d;
                    }
                }
            }
        }
        return distance;
    }

    public static Segment normalizeVector(Segment v ){
        if( almostEqual(v.x * v.x + v.y * v.y , 1)){
            return v;
        }
        double len = Math.sqrt(v.x * v.x + v.y *v.y);
        double inverse = 1/len;

        return new Segment(v.x * inverse , v.y * inverse);
    }

    public static Double segmentDistance (Segment A ,Segment B ,Segment E ,Segment F ,Segment direction ){
        double SEGTOL = 10E-4;
        Segment normal = new Segment( direction.y , - direction.x );

        Segment reverse = new Segment( -direction.x , -direction.y );

        double dotA = A.x*normal.x + A.y*normal.y;
        double dotB = B.x*normal.x + B.y*normal.y;
        double dotE = E.x*normal.x + E.y*normal.y;
        double dotF = F.x*normal.x + F.y*normal.y;

        double crossA = A.x*direction.x + A.y*direction.y;
        double crossB = B.x*direction.x + B.y*direction.y;
        double crossE = E.x*direction.x + E.y*direction.y;
        double crossF = F.x*direction.x + F.y*direction.y;
        double crossABmin = Math.min(crossA,crossB);
        double crossABmax = Math.max(crossA,crossB);

        double crossEFmax = Math.max(crossE,crossF);
        double crossEFmin = Math.min(crossE,crossF);

        double ABmin = Math.min(dotA,dotB);
        double ABmax = Math.max(dotA,dotB);

        double EFmax = Math.max(dotE,dotF);
        double EFmin = Math.min(dotE,dotF);

        if(almostEqual(ABmax, EFmin,SEGTOL) || almostEqual(ABmin, EFmax,SEGTOL)){
            return null;
        }
        if(ABmax < EFmin || ABmin > EFmax){
            return null;
        }
        double overlap ;
        if((ABmax > EFmax && ABmin < EFmin) || (EFmax > ABmax && EFmin < ABmin)){
            overlap = 1;
        }
        else{
            double minMax = Math.min(ABmax, EFmax);
            double maxMin = Math.max(ABmin, EFmin);

            double maxMax = Math.max(ABmax, EFmax);
            double minMin = Math.min(ABmin, EFmin);

            overlap = (minMax-maxMin)/(maxMax-minMin);
        }
        double crossABE = (E.y - A.y) * (B.x - A.x) - (E.x - A.x) * (B.y - A.y);
        double crossABF = (F.y - A.y) * (B.x - A.x) - (F.x - A.x) * (B.y - A.y);

        if(almostEqual(crossABE,0) && almostEqual(crossABF,0)){

            Segment ABnorm = new Segment(B.y - A.y , A.x -B.x);
            Segment EFnorm = new Segment(F.y-E.y, E.x-F.x);

            double ABnormlength = Math.sqrt(ABnorm.x*ABnorm.x + ABnorm.y*ABnorm.y);
            ABnorm.x /= ABnormlength;
            ABnorm.y /= ABnormlength;

            double EFnormlength = Math.sqrt(EFnorm.x*EFnorm.x + EFnorm.y*EFnorm.y);
            EFnorm.x /= EFnormlength;
            EFnorm.y /= EFnormlength;

            if(Math.abs(ABnorm.y * EFnorm.x - ABnorm.x * EFnorm.y) < SEGTOL && ABnorm.y * EFnorm.y + ABnorm.x * EFnorm.x < 0){
                double normdot = ABnorm.y * direction.y + ABnorm.x * direction.x;
                if(almostEqual(normdot,0, SEGTOL)){
                    return null;
                }
                if(normdot < 0){
                    return (double)0;
                }
            }
            return null;
        }
        List<Double> distances = new ArrayList<Double>();

        if(almostEqual(dotA, dotE)){
            distances.add(crossA-crossE);
        }
        else if(almostEqual(dotA, dotF)){
            distances.add(crossA-crossF);
        }
        else if(dotA > EFmin && dotA < EFmax){
            Double d = pointDistance(A,E,F,reverse ,false);
            if(d != null && almostEqual(d, 0)){
                Double dB = pointDistance(B,E,F,reverse,true);
                if(dB < 0 || almostEqual(dB*overlap,0)){
                    d = null;
                }
            }
            if(d != null){
                distances.add(d);
            }
        }

        if(almostEqual(dotB, dotE)){
            distances.add(crossB-crossE);
        }
        else if(almostEqual(dotB, dotF)){
            distances.add(crossB-crossF);
        }
        else if(dotB > EFmin && dotB < EFmax){
            Double d = pointDistance(B,E,F,reverse , false);

            if(d != null && almostEqual(d, 0)){
                Double dA = pointDistance(A,E,F,reverse,true);
                if(dA < 0 || almostEqual(dA*overlap,0)){
                    d = null;
                }
            }
            if(d != null){
                distances.add(d);
            }
        }

        if(dotE > ABmin && dotE < ABmax){
            Double d = pointDistance(E,A,B,direction ,false);
            if(d != null && almostEqual(d, 0)){
                Double dF = pointDistance(F,A,B,direction, true);
                if(dF < 0 || almostEqual(dF*overlap,0)){
                    d = null;
                }
            }
            if(d != null){
                distances.add(d);
            }
        }

        if(dotF > ABmin && dotF < ABmax){
            Double d = pointDistance(F,A,B,direction ,false);
            if(d != null && almostEqual(d, 0)){
                Double dE = pointDistance(E,A,B,direction, true);
                if(dE < 0 || almostEqual(dE*overlap,0)){
                    d = null;
                }
            }
            if(d != null){
                distances.add(d);
            }
        }

        if(distances.size() == 0){
            return null;
        }

        Double minElement = Double.MAX_VALUE;
        for(Double d : distances){
            if( d < minElement ){
                minElement = d;
            }
        }
        return minElement;
    }

    public static Double pointDistance( Segment p ,Segment s1 , Segment s2 ,Segment normal , Boolean infinite){
        normal = normalizeVector(normal);
        Segment dir = new Segment(normal.y , - normal.x );

        double pdot = p.x*dir.x + p.y*dir.y;
        double s1dot = s1.x*dir.x + s1.y*dir.y;
        double s2dot = s2.x*dir.x + s2.y*dir.y;

        double pdotnorm = p.x*normal.x + p.y*normal.y;
        double s1dotnorm = s1.x*normal.x + s1.y*normal.y;
        double s2dotnorm = s2.x*normal.x + s2.y*normal.y;


        if(infinite == null || !infinite){
            if (((pdot<s1dot || almostEqual(pdot, s1dot)) && (pdot<s2dot || almostEqual(pdot, s2dot))) || ((pdot>s1dot || almostEqual(pdot, s1dot)) && (pdot>s2dot || almostEqual(pdot, s2dot)))){
                return null;
            }
            if ((almostEqual(pdot, s1dot) && almostEqual(pdot, s2dot)) && (pdotnorm>s1dotnorm && pdotnorm>s2dotnorm)){
                return Math.min(pdotnorm - s1dotnorm, pdotnorm - s2dotnorm);
            }
            if ((almostEqual(pdot, s1dot) && almostEqual(pdot, s2dot)) && (pdotnorm<s1dotnorm && pdotnorm<s2dotnorm)){
                return -Math.min(s1dotnorm-pdotnorm, s2dotnorm-pdotnorm);
            }
        }

        return -(pdotnorm - s1dotnorm + (s1dotnorm - s2dotnorm)*(s1dot - pdot)/(s1dot - s2dot));
    }

    public static NestPath linearize(Segment p1 , Segment p2 , double rx , double ry , double angle ,int laregearc , int sweep , double tol ){
        NestPath finished = new NestPath();
        finished.add(p2);
        DataExchange arc = ConvertToCenter(p1,p2,rx,ry,angle,laregearc,sweep);
        Deque<DataExchange> list = new ArrayDeque<>();
        list.add(arc);
        while(list.size() > 0 ){
            arc = list.getFirst();
            DataExchange fullarc = ConvertToSvg(arc.center,arc.rx , arc.ry ,arc.theta , arc.extent , arc.angle);
            DataExchange subarc = ConvertToSvg(arc.center , arc.rx ,arc.ry ,arc.theta ,0.5*arc.extent , arc.angle);
            Segment arcmid = subarc.p2;
            Segment mid = new Segment(0.5*(fullarc.p1.x + fullarc.p2.x) , 0.5 *(fullarc.p1.y + fullarc.p2.y));
            if(withinDistance( mid , arcmid ,tol )){
                finished.reverse();finished.add(new Segment(fullarc.p2));finished.reverse();
                list.removeFirst();
            }
            else{
                DataExchange arc1 = new DataExchange(new Segment(arc.center), arc.rx, arc.ry , arc.theta , 0.5 * arc.extent , arc.angle , false);
                DataExchange arc2 = new DataExchange(new Segment(arc.center),arc.rx , arc.ry , arc.theta+0.5 * arc.extent , 0.5 * arc.extent , arc.angle , false);
                list.removeFirst();
                list.addFirst(arc2);list.addFirst(arc1);
            }
        }
        return finished;
    }

    public static DataExchange ConvertToSvg(Segment center , double rx , double ry , double theta1 , double extent , double angleDegrees){
        double theta2 = theta1 + extent;

        theta1 = degreesToRadians(theta1);
        theta2 = degreesToRadians(theta2);
        double angle = degreesToRadians(angleDegrees);

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double t1cos = Math.cos(theta1);
        double t1sin = Math.sin(theta1);

        double t2cos = Math.cos(theta2);
        double t2sin = Math.sin(theta2);

        double x0 = center.x + cos * rx * t1cos +	(-sin) * ry * t1sin;
        double y0 = center.y + sin * rx * t1cos +	cos * ry * t1sin;

        double x1 = center.x + cos * rx * t2cos +	(-sin) * ry * t2sin;
        double y1 = center.y + sin * rx * t2cos +	cos * ry * t2sin;

        int largearc = (extent > 180) ? 1 : 0;
        int sweep = (extent > 0) ? 1 : 0;
        List<Segment> list = new ArrayList<>();
        list.add(new Segment(x0,y0));list.add(new Segment(x1,y1));
        return new DataExchange(new Segment(x0,y0), new Segment(x1,y1),rx,ry,angle , largearc , sweep , true);
    }

    public static DataExchange ConvertToCenter(Segment p1 , Segment p2 , double rx , double ry , double angleDgrees , int largearc , int sweep){
        Segment mid = new Segment(0.5 *(p1.x +p2.x) ,0.5 *(p1.y +p2.y));
        Segment diff = new Segment(0.5 *(p2.x - p1.x ) , 0.5 * (p2.y - p1.y ));

        double angle = degreesToRadians(angleDgrees);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double x1 = cos * diff.x + sin * diff.y;
        double y1 = -sin * diff.x + cos * diff.y;

        rx = Math.abs(rx);
        ry = Math.abs(ry);
        double Prx = rx * rx;
        double Pry = ry * ry;
        double Px1 = x1 * x1;
        double Py1 = y1 * y1;

        double radiiCheck = Px1/Prx + Py1/Pry;
        double radiiSqrt = Math.sqrt(radiiCheck);
        if (radiiCheck > 1) {
            rx = radiiSqrt * rx;
            ry = radiiSqrt * ry;
            Prx = rx * rx;
            Pry = ry * ry;
        }

        double sign = (largearc != sweep) ? -1 : 1;
        double sq = ((Prx * Pry) - (Prx * Py1) - (Pry * Px1)) / ((Prx * Py1) + (Pry * Px1));

        sq = (sq < 0) ? 0 : sq;

        double coef = sign * Math.sqrt(sq);
        double cx1 = coef * ((rx * y1) / ry);
        double cy1 = coef * -((ry * x1) / rx);

        double cx = mid.x + (cos * cx1 - sin * cy1);
        double cy = mid.y + (sin * cx1 + cos * cy1);

        double ux = (x1 - cx1) / rx;
        double uy = (y1 - cy1) / ry;
        double vx = (-x1 - cx1) / rx;
        double vy = (-y1 - cy1) / ry;
        double n = Math.sqrt( (ux * ux) + (uy * uy) );
        double p = ux;
        sign = (uy < 0) ? -1 : 1;

        double theta = sign * Math.acos( p / n );
        theta = radiansToDegree(theta);

        n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = ((ux * vy - uy * vx) < 0) ? -1 : 1;
        double delta = sign * Math.acos( p / n );
        delta = radiansToDegree(delta);

        if (sweep == 1 && delta > 0)
        {
            delta -= 360;
        }
        else if (sweep == 0 && delta < 0)
        {
            delta += 360;
        }
        delta %= 360;
        theta %= 360;
        List<Segment> list = new ArrayList<>();
        list.add(new Segment(cx , cy ));
        return new DataExchange(new Segment(cx,cy) , rx ,ry , theta , delta , angleDgrees , false);

    }

    public static double degreesToRadians(double angle){
        return angle * (Math.PI / 180);
    }

    public static double radiansToDegree(double angle){
        return angle * ( 180 / Math.PI);
    }

    static class DataExchange{
        Segment p1;
        Segment p2;
        Segment center;
        double rx;
        double ry;
        double theta;
        double extent;
        double angle;
        double largearc;
        int sweep;
        boolean flag;

        public DataExchange(Segment p1, Segment p2, double rx, double ry, double angle, double largearc, int sweep ,boolean flag) {
            this.p1 = p1;
            this.p2 = p2;
            this.rx = rx;
            this.ry = ry;
            this.angle = angle;
            this.largearc = largearc;
            this.sweep = sweep;
            this.flag = flag;
        }

        public DataExchange(Segment center, double rx, double ry, double theta, double extent, double angle , boolean flag) {
            this.center = center;
            this.rx = rx;
            this.ry = ry;
            this.theta = theta;
            this.extent = extent;
            this.angle = angle;
            this.flag = flag;
        }

        @Override
        public String toString() {
            String s = "";
            if(flag){
                s += " p1 = " +p1.toString() +" p2 = "+ p2.toString() +"\n rx = "+ rx +" ry = "+ry +" angle = "+angle +" largearc = "+largearc +" sweep = "+ sweep ;
            }
            else{
                s += " center = "+center +"\n rx = "+ rx +" ry = "+ ry +" theta = "+ theta +" extent = "+ extent +" angle = "+ angle ;
            }
            return s;
        }
    }

    public static boolean withinDistance( Segment p1 , Segment p2 , double distance){
        double dx = p1.x - p2.x ;
        double dy = p1.y - p2.y ;
        return ((dx * dx + dy * dy) < distance * distance);
    }

}
