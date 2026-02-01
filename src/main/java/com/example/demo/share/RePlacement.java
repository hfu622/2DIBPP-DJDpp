package com.example.demo.share;

import com.example.demo.de.lighti.clipper.*;
import com.example.demo.nest.data.Bound;
import com.example.demo.nest.data.NestPath;
import com.example.demo.nest.data.Segment;
import com.example.demo.nest.util.CommonUtil;
import com.example.demo.nest.util.Config;
import com.example.demo.nest.util.GeometryUtil;
import com.example.demo.nest.util.coor.ClipperCoor;

import java.util.*;

public class RePlacement {

    public int xObject;
    public int yObject;

    public Queue tabuQueue;

    public double threshold;
    public double smallThreshold;

    public List<String> resultUion = new ArrayList<>();

    public List<Map<String, Double>> processUion = new ArrayList<>();

    public List<List<NestPath>> pieceList = new ArrayList<>();

    public List<Paths> holeList = new ArrayList<>();
    
    public List<Sheet> objectList = new ArrayList<>();

    public List<Piece> originPieceList = new ArrayList<>();

    List<NestPath> convexList = new ArrayList<>();
    List<NestPath> nonConvexList = new ArrayList<>();

    public Map<Integer, Integer> pieceInObjectMap = new HashMap<>();

    public void setObjectList(List<Sheet> listaObjetos) {
        this.objectList = listaObjetos;
    }

    public void setOriginPieceList(List<Piece> originPieceList) {
        this.originPieceList = originPieceList;
    }

    public void fillSingleHole(List<Sheet> listaObjetos){
        objectList = listaObjetos;
        Collections.sort(objectList);

        updatePieceList();

        updateHole();

        for(int i = 0; i < pieceList.size(); i++){
            if(objectList.get(i).getFreeArea() == 0){
                continue;
            }
            for(int k = 0; k < pieceList.get(i).size(); k++){
                boolean is_end = false;
                NestPath path = pieceList.get(i).get(k);
                NestPath origin = path;
                List<NestPath> f = new ArrayList<>();
                for(int p = 0; p < holeList.size(); p++){
                    if(p==i){
                        continue;
                    }
                    if(is_end){
                        break;
                    }
                    Paths remain = holeList.get(p);

                    List<NestPath> holeNFP = new ArrayList<>();
                    for(int j = 0 ; j<remain.size() ; j++){
                        holeNFP.add( toNestCoordinates(remain.get(j)));
                    }
                    Collections.sort(holeNFP);

                    NestPath enableHoleNfp = new NestPath();
                    for(int u = 0; u < holeNFP.size(); u++){
                        if(is_end){
                            break;
                        }
                        if( GeometryUtil.almostEqual(Math.abs(GeometryUtil.polygonArea(holeNFP.get(u))), Math.abs(GeometryUtil.polygonArea(path)))  ){
                            enableHoleNfp = holeNFP.get(u);
                            for(int rotateNum = 0; rotateNum < 4; rotateNum++){
                                if(is_end){
                                    break;
                                }
                                int rotation = (path.getRotation()+90) % 360;
                                path = GeometryUtil.rotatePolygon2Polygon(origin , rotation);
                                path.setRotation(rotation);
                                path.setSource(path.getSource());
                                path.setId( path.getId());
                                path.setArea(path.getArea());

                                for(int t = 0; t < enableHoleNfp.size(); t++){
                                    if(is_end){
                                        break;
                                    }
                                    for(int positionPoint = 0; positionPoint < path.size(); positionPoint++) {
                                        if(is_end){
                                            break;
                                        }
                                        double shifx = enableHoleNfp.get(t).x - path.get(positionPoint).x;
                                        double shify = enableHoleNfp.get(t).y - path.get(positionPoint).y;

                                        NestPath shifPath = new NestPath();

                                        for (int m = 0; m < path.size(); m++) {
                                            shifPath.add(new Segment(Math.rint(path.get(m).x + shifx), Math.rint(path.get(m).y + shify)));
                                        }

                                        if (GeometryUtil.insidePP(shifPath, enableHoleNfp)) {
                                            NestPath point = new NestPath();
                                            point.add(enableHoleNfp.get(t).x, enableHoleNfp.get(t).y);
                                            f.add(point);
                                            movePiecetoObject(i,k,p,rotation,shifPath,holeList,pieceList,objectList);
                                            is_end = true;
                                            k = -1;
                                            break;
                                        }
                                    }
                                }

                            }

                        }
                    }
                }
            }
        }

        Collections.sort(objectList);
    }

    public void changeOne(){
        Collections.sort(objectList);
        updatePieceList();
        updateHole();
        updatePieceInObjectMap();

        List<Sheet> fullObjects = new ArrayList<>();
        List<Paths> fullHoleList = new ArrayList<>();
        List<List<NestPath>> fullPieceList = new ArrayList<>();
        for(int i = objectList.size() - 1; i >= 0; i--){
            if(objectList.get(i).getFreeArea() == 0){
                Sheet removeSheet = objectList.remove(i);
                fullObjects.add(removeSheet);
                Paths removeHole = holeList.remove(i);
                fullHoleList.add(removeHole);
                List<NestPath> removePieceList = pieceList.remove(i);
                fullPieceList.add(removePieceList);
            }else{
                List<Piece> pzasInside = objectList.get(i).getPzasInside();
                Collections.sort(pzasInside);

                Collections.sort(pieceList.get(i));
            }
        }

        for(int i = objectList.size() - 1; i >= objectList.size() - 2; i--){
            List<Piece> pzasInside = objectList.get(i).getPzasInside();
            boolean canPlace = false;
            for(int j = 0; j < pzasInside.size(); j++){
                if(canPlace){
                    break;
                }
                Piece piece = pzasInside.get(j);
                int holeNum = holeList.get(i).size();
                NestPath nestPath = PiecetoNestPath(piece);
                Path piecePath = scaleUp2ClipperCoordinates(nestPath);

                Paths holes = holeList.get(i);
                Paths newHoles = unionPaths(holes, piecePath);

                if(newHoles.size() > holes.size()){
                    continue;
                }else{
                    NestPath newHole = findChangeHole(holes, newHoles);

                    double putInArea = GeometryUtil.polygonArea(newHole);
                    List<NestPath> fillInPiece = findFillInHolePieces(i, putInArea);

                    if(fillInPiece.size() == 0 || fillInPiece == null){
                        continue;
                    }
                    if(fillInPiece.get(0).bid == i && fillInPiece.size() == 1 && GeometryUtil.insidePP(fillInPiece.get(0), newHole)){
                        continue;
                    }

                    Collections.sort(fillInPiece);

                    for(int k = fillInPiece.size()-1; k >= 0; k--){
                        NestPath movePath = placePieceInHole(newHole, fillInPiece.get(k));
                        if(movePath.size() == 0 || movePath == null){
                            canPlace = false;
                            break;
                        }else{
                            fillInPiece.set(k,movePath);
                            newHole = updateHoleWhenPiecePlaceIn(newHole, movePath);
                            canPlace = true;
                        }
                    }

                    if(canPlace){
                        NestPath removePiece = pieceList.get(i).remove(j);
                        for (NestPath canFillInPath : fillInPiece) {
                            pieceList.get(i).add(canFillInPath);
                        }
                        for (NestPath canFillInPath : fillInPiece) {
                            int objectId = pieceInObjectMap.get(canFillInPath.getId());
                            for (NestPath prepareRemovePath : pieceList.get(objectId)) {
                                if(prepareRemovePath.getId() == canFillInPath.getId()){
                                    pieceList.get(objectId).remove(prepareRemovePath);
                                    break;
                                }
                            }
                        }

                        List<NestPath> newobjectPiece = new ArrayList<>();
                        newobjectPiece.add(removePiece);
                        pieceList.add(0,newobjectPiece);
                        changePiecelistToObjcelist();

                    }

                }

            }

        }
        for (Sheet fullObject : fullObjects) {
            objectList.add(fullObject);
        }
        for (Paths paths : fullHoleList) {
            holeList.add(paths);
        }
        for (List<NestPath> nestPaths : fullPieceList) {
            pieceList.add(nestPaths);
        }

        updateHole();

        updatePieceInObjectMap();
        Collections.sort(objectList);
    }

    public void noHolePlace(){
        pieceList.clear();
        holeList.clear();
        pieceInObjectMap.clear();
        List<NestPath> alreadyPieceList = new ArrayList<>();
        List<NestPath> fullPieceList = new ArrayList<>();
        for(int i = 0; i < objectList.size(); i++){
            if(objectList.get(i).getFreeArea() == 0){
                for (Piece piece : objectList.get(i).getPzasInside()) {
                    fullPieceList.add(PiecetoNestPath(piece));
                }
            }
            for (Piece piece : objectList.get(i).getPzasInside()) {
                alreadyPieceList.add(PiecetoNestPath(piece));
            }
        }
        Collections.sort(alreadyPieceList);

        while(!alreadyPieceList.isEmpty()){
            NestPath pointsList = new NestPath();
            NestPath path = alreadyPieceList.get(alreadyPieceList.size() - 1);

        }

    }

    public void movePiecetoObject(int i, int k, int p, int rotate, NestPath shifPath, List<Paths> holeList, List<List<NestPath>> pieceList, List<Sheet> objectList) {
        NestPath remove = pieceList.get(i).remove(k);
        objectList.get(i).getPzasInside().remove(k);
        shifPath.setRotation(rotate);
        shifPath.setId(remove.getId());
        shifPath.setArea(remove.getArea());
        pieceList.get(p).add(shifPath);

        int vertices = shifPath.size();
        int[] coordenadas = new int[vertices*2];
        List<Segment> segments = shifPath.getSegments();
        int cnt = 0;
        for(int u = 0; u < vertices; u++){
            coordenadas[cnt++] = (int) segments.get(u).x;
            coordenadas[cnt++] = (int) segments.get(u).y;
        }
        Piece piece = new Piece(coordenadas);
        piece.setnumber(remove.getId());
        piece.setRotada(rotate);
        piece.setArea((int) remove.getArea());
        objectList.get(p).addPieza(piece);

        Paths currentPath = holeList.remove(p);
        Paths shifPaths = new Paths();
        shifPaths.add(scaleUp2ClipperCoordinates(shifPath));

        Paths remainOne = new Paths();
        DefaultClipper clipper1 = new DefaultClipper(2);
        clipper1.addPaths(shifPaths, Clipper.PolyType.CLIP, true);
        clipper1.addPaths(currentPath, Clipper.PolyType.SUBJECT, true);
        clipper1.execute(Clipper.ClipType.DIFFERENCE, remainOne, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);

        holeList.add(p, remainOne);

    }

    public void findHole(List<List<NestPath>> pieceList){
        holeList.clear();
        for(int i = 0; i < pieceList.size(); i++){
            NestPath bin = new NestPath();
            bin.add(0, 0);
            bin.add(1000, 0);
            bin.add(1000, 1000);
            bin.add(0, 1000);
            Paths polygonBin = new Paths();
            polygonBin.add(scaleUp2ClipperCoordinates(bin));

            Paths remain = new Paths();
            DefaultClipper clipperLast = new DefaultClipper(2);

            for(int j = 0; j < pieceList.get(i).size();j++){
                NestPath currentPath = pieceList.get(i).get(j);

                Paths remainOne = new Paths();
                Path clone = scaleUp2ClipperCoordinates(currentPath);
                DefaultClipper clipper1 = new DefaultClipper(2);
                clipper1.addPath(clone, Clipper.PolyType.CLIP, true);
                clipper1.addPath(polygonBin.get(0), Clipper.PolyType.SUBJECT, true);
                clipper1.execute(Clipper.ClipType.DIFFERENCE, remainOne, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);

                if(j == 0){
                    clipperLast.addPaths(remainOne , Clipper.PolyType.CLIP , true);
                    remain = remainOne;
                }else{
                    clipperLast.addPaths(remainOne , Clipper.PolyType.SUBJECT , true);
                    clipperLast.execute(Clipper.ClipType.INTERSECTION, remain, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);
                    clipperLast = new DefaultClipper(2);
                    clipperLast.addPaths(remain , Clipper.PolyType.CLIP , true);
                }
            }

            if(remain.size() == 0){
                clipperLast.addPaths(remain, Clipper.PolyType.SUBJECT, true);
                clipperLast.addPaths(polygonBin, Clipper.PolyType.CLIP, true);
                clipperLast.execute(Clipper.ClipType.INTERSECTION, remain, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);
            }

            holeList.add(remain);
        }
    }

    public static Path scaleUp2ClipperCoordinates(NestPath polygon){
        Path p = new Path();
        for(Segment s : polygon.getSegments()){
            ClipperCoor cc = CommonUtil.toClipperCoor(s.x , s.y);
            p.add(new Point.LongPoint(cc.getX() , cc.getY()));
        }
        return p;
    }

    public static NestPath toNestCoordinates(Path polygon){
        NestPath clone = new NestPath();
        for(int i = 0 ; i< polygon.size() ; i ++){
            Segment s = new Segment((double)polygon.get(i).getX()/ Config.CLIIPER_SCALE , (double)polygon.get(i).getY()/Config.CLIIPER_SCALE);
            clone.add(s);
        }
        return clone ;
    }

    public static NestPath PiecetoNestPath(Piece piece){
        NestPath path = new NestPath();
        double x,y;
        int vertices = piece.getvertices();
        for(int j = 0; j < vertices; j++){
            x = (double)piece.coordX[j];
            y = (double)piece.coordY[j];
            path.add(x,y);
        }
        path.setId(piece.getnumber());
        path.setRotation((int)piece.getRotada());
        path.setArea(piece.getArea());
        if(piece.getStrPid().equals("&")){
            path.setStrId(String.valueOf(piece.getnumber()));
        }else{
            path.setStrId(piece.getStrPid());
        }

        return path;
    }

    public static Piece NestPathtoPiece(NestPath path){
        int vertices = path.size();
        int[] coordinates = new int[vertices*2];
        int cnt = 0;
        for(int i = 0; i < vertices; i++){
            coordinates[cnt++] = (int) path.get(i).x;
            coordinates[cnt++] = (int) path.get(i).y;
        }
        Piece piece = new Piece(coordinates);
        piece.setnumber(path.getId());
        piece.setRotada(path.getRotation());
        piece.setArea((int) path.getArea());
        piece.setStrPid(path.getStrId());
        return piece;
    }

    public static Piece NestPathtoPieceIncludeChild(NestPath path){
        int vertices = path.size();
        int[] coordinates = new int[vertices*2];
        int cnt = 0;
        for(int i = 0; i < vertices; i++){
            coordinates[cnt++] = (int) path.get(i).x;
            coordinates[cnt++] = (int) path.get(i).y;
        }
        Piece piece = new Piece(coordinates);
        piece.setnumber(path.getId());
        piece.setRotada(path.getRotation());
        piece.setArea((int) path.getArea());
        piece.setStrPid(path.getStrId());

        piece = dfs(piece, path);

        return piece;
    }

    public static Piece dfs(Piece piece, NestPath path){
        piece = NestPathtoPiece(path);
        if(path.child.size() > 0){
            piece.child.add(dfs(new Piece(), path.child.get(0)));
            piece.child.add(dfs(new Piece(), path.child.get(1)));
        }

        return piece;
    }

    public static void preOrder(NestPath path, List<Piece> children){
        if(path.leftChild == null && path.rightChild == null){
            children.add(NestPathtoPiece(path));
        }
        if(path.leftChild != null && path.rightChild != null){
            preOrder(path.leftChild, children);
            preOrder(path.rightChild, children);
        }
    }

    public static Paths unionPaths(Paths paths, Path path){
        Paths remainOne = new Paths();
        DefaultClipper clipper1 = new DefaultClipper(2);
        clipper1.addPaths(paths, Clipper.PolyType.CLIP, true);
        clipper1.addPath(path, Clipper.PolyType.SUBJECT, true);
        clipper1.execute(Clipper.ClipType.UNION, remainOne, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);
        return remainOne;
    }

    public static Paths unionPath(Path path1, Path path2){
        Paths remainOne = new Paths();
        DefaultClipper clipper1 = new DefaultClipper();
        clipper1.addPath(path1, Clipper.PolyType.CLIP, true);
        clipper1.addPath(path2, Clipper.PolyType.SUBJECT, true);
        clipper1.execute(Clipper.ClipType.UNION, remainOne, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);
        return remainOne;
    }

    private List<NestPath> findFillInHolePieces(int i, double putInArea) {
        List<NestPath> fillInPiece = new ArrayList<>();
        for(int k = 0; k < pieceList.size(); k++){
            for (NestPath otherObjctNestPath : pieceList.get(k)) {
                otherObjctNestPath.bid = k;
                if(GeometryUtil.polygonArea(otherObjctNestPath) > putInArea){
                    break;
                }else if(GeometryUtil.polygonArea(otherObjctNestPath) == putInArea){
                    fillInPiece.add(otherObjctNestPath);
                    return fillInPiece;
                }
            }
        }
        List<NestPath> result = new ArrayList<>();
        List<NestPath> newPieceList = new ArrayList<>();
        for(int k = 0; k < pieceList.size(); k++){
            for (NestPath nestPath : pieceList.get(k)) {
                nestPath.bid = k;
                newPieceList.add(nestPath);
            }
        }
        Collections.sort(newPieceList);
        if(findTwoPieceFillInHole(newPieceList,putInArea,0,0,result)){
            return result;
        }

        result.clear();
        newPieceList.clear();
        for(int k = 0; k < pieceList.size(); k++){
            for (NestPath nestPath : pieceList.get(k)) {
                nestPath.bid = k;
                newPieceList.add(nestPath);
            }
        }
        Collections.sort(newPieceList);
        if(findThreePieceFillInHole(newPieceList,putInArea,0,0,result)){
            return result;
        }

        result.clear();
        newPieceList.clear();
        for(int k = 0; k < pieceList.size(); k++){
            for (NestPath nestPath : pieceList.get(k)) {
                nestPath.bid = k;
                newPieceList.add(nestPath);
            }
        }
        Collections.sort(newPieceList);
        if(findFourPieceFillInHole(newPieceList,putInArea,0,0,result)){
            return result;
        }
        return fillInPiece;
    }

    public NestPath findChangeHole(Paths holes, Paths newHoles) {
        List<NestPath> originHoleList = new ArrayList<>();
        for (Path everyOriginHole : holes) {
            originHoleList.add(toNestCoordinates(everyOriginHole));
        }
        List<NestPath> newHoleList = new ArrayList<>();
        for (Path everyNewHole : newHoles) {
            newHoleList.add(toNestCoordinates(everyNewHole));
        }
        NestPath newHole = new NestPath();
        for (NestPath everyNewHole : newHoleList) {
            boolean flag = true;
            for(int i = 0; i < originHoleList.size(); i++){
                NestPath path = originHoleList.get(i);
                if(GeometryUtil.polygonArea(everyNewHole) == GeometryUtil.polygonArea(path)){
                    for(int j = 0; j < originHoleList.size(); j++){
                        if(j==i){
                            continue;
                        }
                        if(GeometryUtil.insidePP(originHoleList.get(j), everyNewHole) && newHoles.size() < holes.size()){
                            return everyNewHole;
                        }
                    }
                    flag = false;
                    break;
                }
            }
            if(flag){
                newHole = everyNewHole;
                return newHole;
            }
        }
        return newHole;
    }

    public NestPath placePieceInHole(NestPath enableHoleNfp, NestPath path){
        NestPath origin = null;
        try {
            origin = path.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        NestPath movePath = new NestPath();
        boolean is_end = false;
        for(int rotateNum = 0; rotateNum <4; rotateNum++){
            if(is_end){
                break;
            }
            int rotation = (path.getRotation()+90) % 360;
            path = GeometryUtil.rotatePolygon2Polygon(origin , rotation);
            path.setRotation(rotation);
            path.setId( path.getId());
            path.setArea(path.getArea());

            for(int t = 0; t < enableHoleNfp.size(); t++){
                if(is_end){
                    break;
                }
                for(int positionPoint = 0; positionPoint < path.size(); positionPoint++) {
                    if(is_end){
                        break;
                    }
                    double shifx = enableHoleNfp.get(t).x - path.get(positionPoint).x;
                    double shify = enableHoleNfp.get(t).y - path.get(positionPoint).y;

                    NestPath shifPath = new NestPath();

                    for (int m = 0; m < path.size(); m++) {
                        shifPath.add(new Segment(Math.rint(path.get(m).x + shifx), Math.rint(path.get(m).y + shify)));
                    }

                    if (GeometryUtil.insidePP(shifPath, enableHoleNfp)) {
                        is_end = true;
                        movePath = shifPath;
                        movePath.setRotation(rotation);
                        movePath.setId( path.getId());
                        movePath.setArea(path.getArea());

                        break;
                    }
                }
            }

        }

        return movePath;
    }

    public void updatePieceList() {
        pieceList.clear();
        for(int i = 0; i < objectList.size(); i++){
            Sheet currentSheet = objectList.get(i);

            List<NestPath> everyObjectPath = new ArrayList<>();
            List<Piece> pieceInside = currentSheet.getPzasInside();
            for (Piece piece : pieceInside) {
                NestPath path = new NestPath();
                double x,y;
                int vertices = piece.getvertices();
                for(int j = 0; j < vertices; j++){
                    x = (double)piece.coordX[j];
                    y = (double)piece.coordY[j];
                    path.add(x,y);
                }
                path.setId(piece.getnumber());
                path.setRotation((int)piece.getRotada());
                path.setArea(piece.getTotalSize());
                everyObjectPath.add(path);
            }
            pieceList.add(everyObjectPath);
        }
    }

    public void updateHole() {
        holeList.clear();
        findHole(pieceList);
    }

    public void updatePieceInObjectMap() {
        pieceInObjectMap.clear();
        for(int i = 0; i < pieceList.size(); i++){
            for (NestPath nestPath : pieceList.get(i)) {
                pieceInObjectMap.put(nestPath.getId(), i);
            }
        }
    }

    public boolean findTwoPieceFillInHole(List<NestPath> piecesList, double area, double pieceAreaSum, int index, List<NestPath> result){
        if(result.size() == 2 && pieceAreaSum == area){
            return true;
        }
        if(result.size() == 2 && pieceAreaSum != area){
            return false;
        }
        for(int i = index; i < piecesList.size(); i++){
            double pieceArea = GeometryUtil.polygonArea(piecesList.get(i));
            if(pieceArea >= area){
                break;
            }
            result.add(piecesList.get(i));
            pieceAreaSum += pieceArea;
            if(findTwoPieceFillInHole(piecesList, area, pieceAreaSum,index+1, result)){
                return true;
            }
            result.remove(result.size()-1);
            pieceAreaSum -= pieceArea;
        }
        return false;
    }

    public boolean findThreePieceFillInHole(List<NestPath> piecesList, double area, double pieceAreaSum, int index, List<NestPath> result){
        if(result.size() == 3 && pieceAreaSum == area){
            return true;
        }
        if(result.size() == 3 && pieceAreaSum != area){
            return false;
        }
        for(int i = index; i < piecesList.size(); i++){
            double pieceArea = GeometryUtil.polygonArea(piecesList.get(i));
            if(pieceArea >= area){
                break;
            }
            result.add(piecesList.get(i));
            pieceAreaSum += pieceArea;
            if(findThreePieceFillInHole(piecesList, area, pieceAreaSum,index+1, result)){
                return true;
            }
            result.remove(result.size()-1);
            pieceAreaSum -= pieceArea;
        }
        return false;
    }

    public boolean findFourPieceFillInHole(List<NestPath> piecesList, double area, double pieceAreaSum, int index, List<NestPath> result){
        if(result.size() == 4 && pieceAreaSum == area){
            return true;
        }
        if(result.size() == 4 && pieceAreaSum != area){
            return false;
        }
        for(int i = index; i < piecesList.size(); i++){
            double pieceArea = GeometryUtil.polygonArea(piecesList.get(i));
            if(pieceArea >= area){
                break;
            }
            result.add(piecesList.get(i));
            pieceAreaSum += pieceArea;
            if(findFourPieceFillInHole(piecesList, area, pieceAreaSum,index+1, result)){
                return true;
            }
            result.remove(result.size()-1);
            pieceAreaSum -= pieceArea;
        }
        return false;
    }

    public NestPath updateHoleWhenPiecePlaceIn(NestPath enableHoleNfp, NestPath path) {
        Paths remainOne = new Paths();
        Path clone = scaleUp2ClipperCoordinates(path);
        Path hole = scaleUp2ClipperCoordinates(enableHoleNfp);
        DefaultClipper clipper1 = new DefaultClipper(2);
        clipper1.addPath(clone, Clipper.PolyType.CLIP, true);
        clipper1.addPath(hole, Clipper.PolyType.SUBJECT, true);
        clipper1.execute(Clipper.ClipType.DIFFERENCE, remainOne, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);

        if(remainOne.size() == 0 || remainOne == null){
            return new NestPath();
        }

        return toNestCoordinates(remainOne.get(0));
    }

    public void changePiecelistToObjcelist() {
        objectList.clear();

        for(int i = 0; i < pieceList.size(); i++){
            Sheet sheet = new Sheet(1000, 1000, i);
            for (NestPath nestPath : pieceList.get(i)) {
                sheet.addPieza(NestPathtoPiece(nestPath));
            }
            objectList.add(sheet);
        }

    }

    public void findConvexAndNonconvex(List<Piece> originPieceList, List<NestPath> convexList, List<NestPath> nonConvexList) {
        for(int j = 0; j < originPieceList.size(); j++){
            Piece piece = originPieceList.get(j);
            NestPath path = PiecetoNestPath(piece);
            if(GeometryUtil.isConvex(path)){
                convexList.add(path);
            }else{
                nonConvexList.add(path);
            }
        }
    }

    public List<Piece> combineConvexAndConcave(List<NestPath> convexList, List<NestPath> nonConvexList) {
        List<Piece> result = new ArrayList<>();
        while(!nonConvexList.isEmpty()) {
            NestPath nonConvex = nonConvexList.get(0);
            int j;
            for (j = 0; j < convexList.size(); j++) {
                NestPath convex = convexList.get(j);

                String smallStrId = convex.getStrId().compareTo(nonConvex.getStrId()) <= 0 ? convex.getStrId() : nonConvex.getStrId();
                String bigStrId = convex.getStrId().compareTo(nonConvex.getStrId()) > 0 ? convex.getStrId() : nonConvex.getStrId();
                String uionStr = smallStrId + "|" + bigStrId;
                if(tabuQueue.contains(uionStr)){
                    continue;
                }

                NestPath newConvex = canCombine(nonConvex, convex);
                if (newConvex != null) {
                    convexList.add(newConvex);
                    nonConvexList.remove(nonConvex);
                    convexList.remove(j);
                    break;
                }
            }
            if(j == convexList.size()){
                nonConvexList.remove(nonConvex);
                result.add(NestPathtoPiece(nonConvex));
            }

        }
        for (NestPath path : convexList) {
            path.setArea(GeometryUtil.polygonArea(path));
            result.add(NestPathtoPieceIncludeChild(path));
        }
        return result;
    }

    public List<NestPath> combineConvexAndConcave2(List<NestPath> convexList, List<NestPath> nonConvexList) {
        List<NestPath> result = new ArrayList<>();
        while(!nonConvexList.isEmpty()) {
            NestPath nonConvex = nonConvexList.get(0);
            int j;

            for (j = 0; j < convexList.size(); j++) {
                NestPath convex = convexList.get(j);

                String smallStrId = convex.getStrId().compareTo(nonConvex.getStrId()) <= 0 ? convex.getStrId() : nonConvex.getStrId();
                String bigStrId = convex.getStrId().compareTo(nonConvex.getStrId()) > 0 ? convex.getStrId() : nonConvex.getStrId();
                String uionStr = smallStrId + "|" + bigStrId;

                NestPath newConvex = canCombine(nonConvex, convex);
                if (newConvex != null) {
                    convexList.add(newConvex);
                    nonConvexList.remove(nonConvex);
                    convexList.remove(j);
                    break;
                }
            }

            if(j == convexList.size()){
                nonConvexList.remove(nonConvex);
                result.add(nonConvex);
            }
        }

        for (NestPath path : convexList) {
            path.setArea(GeometryUtil.polygonArea(path));
            result.add(path);
        }
        return result;
    }

    public NestPath canCombine(NestPath nonConvex, NestPath convex) {
        if( (nonConvex.getMaxX()-nonConvex.getMinX()) > xObject || (nonConvex.getMaxY()-nonConvex.getMinY()) > yObject
                || (convex.getMaxX()-convex.getMinX()) > xObject || (convex.getMaxY()-convex.getMinY()) > yObject ){
            return null;
        }

        for(int i = 0; i < nonConvex.size(); i++){
            for(int j = 0; j < convex.size(); j++){
                double shifx = nonConvex.get(i).x - convex.get(j).x;
                double shify = nonConvex.get(i).y - convex.get(j).y;

                NestPath shifPath = new NestPath();
                for (int m = 0; m < convex.size(); m++) {
                    shifPath.add(new Segment(Math.rint(convex.get(m).x + shifx), Math.rint(convex.get(m).y + shify)));
                }

                if(GeometryUtil.interseccionPP(NestPathtoPiece(shifPath), NestPathtoPiece(nonConvex)) || GeometryUtil.intersect(shifPath, nonConvex) || GeometryUtil.intersect(nonConvex, shifPath) || GeometryUtil.insidePP2(shifPath, nonConvex) || GeometryUtil.insidePP2(nonConvex, shifPath) ){
                    continue;
                }else{
                    Paths combinePath = unionPath(scaleUp2ClipperCoordinates(shifPath), scaleUp2ClipperCoordinates(nonConvex));
                    if(combinePath.size() > 1){
                        continue;
                    }
                    NestPath newConvex = toNestCoordinates(combinePath.get(0));

                    if((newConvex.getMaxX()-newConvex.getMinX()) > xObject || (newConvex.getMaxY()-newConvex.getMinY()) > yObject){
                        continue;
                    }

                    if( GeometryUtil.polygonArea(newConvex) != GeometryUtil.polygonArea(nonConvex) && GeometryUtil.polygonArea(newConvex) != GeometryUtil.polygonArea(convex)){
                        long nonConvexId = nonConvex.getLongId();
                        long convexId = convex.getLongId();
                        String id1 = String.valueOf(nonConvexId);
                        String id2 = String.valueOf(convexId);
                        String newId = id1 + id2;
                        String smallStrId = convex.getStrId().compareTo(nonConvex.getStrId()) <= 0 ? convex.getStrId() : nonConvex.getStrId();
                        String bigStrId = convex.getStrId().compareTo(nonConvex.getStrId()) > 0 ? convex.getStrId() : nonConvex.getStrId();
                        String uionStr = smallStrId + "|" + bigStrId;
                        newConvex.setStrId(smallStrId + "&" + bigStrId);

                        double fitness = calculateFitness(newConvex, nonConvex, convex, shifPath);
                        if(fitness >= threshold){
                            Map<String, Double> temp = new HashMap<>();
                            temp.put(uionStr, fitness);
                            processUion.add(temp);

                            shifPath.setId(convex.getId());
                            shifPath.setArea(convex.getArea());
                            shifPath.setChild(convex.getChild());
                            shifPath.setStrId(convex.getStrId());
                            shifPath.leftChild = convex.leftChild;
                            shifPath.rightChild = convex.rightChild;
                            moveChildofPath(convex, shifx, shify);
                            newConvex.leftChild = convex;
                            newConvex.rightChild = nonConvex;
                            newConvex.child.add(convex);
                            newConvex.child.add(nonConvex);

                            return newConvex;
                        }

                    }else{
                        continue;
                    }
                }
            }
        }
        return null;
    }

    private NestPath rotateShifPath(NestPath shifPath, int j, NestPath nonConvex, int i) {
        int n = shifPath.size();
        Segment shifPoint = shifPath.get(j);
        Segment prev1 = shifPath.get((j - 1) % n);
        Segment succ1 = shifPath.get((j + 1) % n);

        Segment nonPoint = nonConvex.get(i);
        Segment prev2 = nonConvex.get((i - 1) % n);
        Segment succ2 = nonConvex.get((i + 1) % n);

        NestPath newPath = null;
        double angle = 0;
        Segment[][] array = {{prev1, prev2}, {prev1, succ2}, {succ1, prev2}, {succ1, succ2}};
        for(int k = 0; k < 4; k++){
            angle = GeometryUtil.angleBetweenLines(shifPoint, array[k][0], nonPoint, array[k][1]);
            newPath = GeometryUtil.rotatePolygon2Polygon(shifPath, (int) angle);
            if(GeometryUtil.intersect(newPath, nonConvex) || GeometryUtil.insidePP2(newPath, nonConvex)){
                return newPath;
            }
        }
        return newPath;
    }

    private double calculateFitness(NestPath newConvex, NestPath nonConvex, NestPath convex, NestPath shifPath) {
        double fitness = 0;
        int numOfNewConvex = newConvex.size();
        int numOfNonConvex = Math.max(nonConvex.size(), convex.size());
        fitness += numOfNewConvex - numOfNonConvex <= 0 ? numOfNonConvex - numOfNewConvex + 1 : 0;
        double edgeOverlapRatio = GeometryUtil.adjancencyAllEdgeRatioPP(nonConvex, shifPath);
        fitness += edgeOverlapRatio;
        fitness += Math.pow(GeometryUtil.polygonArea(newConvex)/GeometryUtil.getPolygonBounds(newConvex).getArea(), 2);
        return fitness;
    }

    private double calculateAllEdgeOverlapRatio(NestPath nonConvex, NestPath shifPath) {
        double ratio = 0;
        List<Segment> segmentsNon = nonConvex.getSegments();
        List<Segment> segmentsShif = shifPath.getSegments();
        for(int i = 0; i < segmentsNon.size(); i++){
            Segment seg1 = segmentsNon.get(i);
            for(int j = 0; j < segmentsShif.size(); j++){
                Segment seg2 = segmentsShif.get(j);
                ratio += calculateSingleEdgeOverlapRatio(seg1, seg2);
            }
        }
        return ratio;
    }

    private double calculateSingleEdgeOverlapRatio(Segment seg1, Segment seg2) {
        return 0;
    }

    public List<Piece> combineNonConvex(){
        List<NestPath> convexList = new ArrayList<>();
        List<NestPath> nonConvexList = new ArrayList<>();
        findConvexAndNonconvex(originPieceList, convexList, nonConvexList);

        List<NestPath> result = combineConvexAndConcave2(convexList, nonConvexList);

        List<Piece> pieceResult = rectangularity2(result);

        return pieceResult;
    }

    public List<Piece> rectangularity(List<Piece> result) {
        List<NestPath> newPieceList = new ArrayList<>();
        for(int i = 0; i < result.size(); i++){
            newPieceList.add(PiecetoNestPath(result.get(i)));
        }
        Collections.sort(newPieceList);

        int iter = 30;
        while(iter > 0){
            boolean canCombine = false;
            for(int i = 0; i < newPieceList.size(); i++){
                if(canCombine){
                    break;
                }
                NestPath ithNestPath = newPieceList.get(i);
                for(int j = newPieceList.size()-1; j >= i+1; j--){
                    if(canCombine){
                        break;
                    }
                    NestPath jthNestPath = newPieceList.get(j);

                    String smallStrId = ithNestPath.getStrId().compareTo(jthNestPath.getStrId()) <= 0 ? ithNestPath.getStrId() : jthNestPath.getStrId();
                    String bigStrId = ithNestPath.getStrId().compareTo(jthNestPath.getStrId()) > 0 ? ithNestPath.getStrId() : jthNestPath.getStrId();
                    String uionStr = smallStrId + "|" + bigStrId;
                    if(tabuQueue.contains(uionStr)){
                        continue;
                    }

                    NestPath newPiece = combineAndImproveRegularity(ithNestPath, jthNestPath);
                    if( newPiece != null && newPiece.size() > 0 ){
                        newPieceList.remove(ithNestPath);
                        newPieceList.remove(jthNestPath);
                        newPieceList.add(newPiece);
                        canCombine = true;
                        break;
                    }
                }
            }
            iter--;
        }

        List<Piece> newResult = new ArrayList<>();
        for(int i = 0; i < newPieceList.size(); i++){
            NestPath path = newPieceList.get(i);
            path.setArea(GeometryUtil.polygonArea(path));
            newResult.add(NestPathtoPieceIncludeChild(path));
        }
        return newResult;
    }

    public List<Piece> rectangularity2(List<NestPath> newPieceList) {

        Collections.sort(newPieceList);

        int iter = 30;
        while(true){
            boolean canCombine = false;
            for(int i = 0; i < newPieceList.size(); i++){
                if(canCombine){
                    break;
                }
                NestPath ithNestPath = newPieceList.get(i);
                for(int j = newPieceList.size()-1; j >= i+1; j--){
                    if(canCombine){
                        break;
                    }
                    NestPath jthNestPath = newPieceList.get(j);

                    String smallStrId = ithNestPath.getStrId().compareTo(jthNestPath.getStrId()) <= 0 ? ithNestPath.getStrId() : jthNestPath.getStrId();
                    String bigStrId = ithNestPath.getStrId().compareTo(jthNestPath.getStrId()) > 0 ? ithNestPath.getStrId() : jthNestPath.getStrId();
                    String uionStr = smallStrId + "|" + bigStrId;

                    NestPath newPiece = combineAndImproveRegularity(ithNestPath, jthNestPath);
                    if( newPiece != null && newPiece.size() > 0 ){
                        newPieceList.remove(ithNestPath);
                        newPieceList.remove(jthNestPath);
                        newPieceList.add(newPiece);
                        canCombine = true;
                        break;
                    }
                }
            }
            if (!canCombine){
                break;
            }
            iter--;
        }

        List<Piece> newResult = new ArrayList<>();
        for(int i = 0; i < newPieceList.size(); i++){
            NestPath path = newPieceList.get(i);
            path.setArea(GeometryUtil.polygonArea(path));
            newResult.add(NestPathtoPieceIncludeChild(path));
        }
        return newResult;
    }

    public NestPath combineAndImproveRegularity(NestPath ithNestPath, NestPath jthNestPath) {
        if(calRegularity(ithNestPath) == 1 || calRegularity(jthNestPath) == 1){
            return null;
        }
        if( (ithNestPath.getMaxX()-ithNestPath.getMinX()) > xObject || (ithNestPath.getMaxY()-ithNestPath.getMinY()) > yObject
            || (jthNestPath.getMaxX()-jthNestPath.getMinX()) > xObject || (jthNestPath.getMaxY()-jthNestPath.getMinY()) > yObject ){
            return null;
        }

        return canCombineRegularity(ithNestPath, jthNestPath);
    }

    public double calRegularity(NestPath path) {
        Bound polygonBounds = GeometryUtil.getPolygonBounds(path);
        return (GeometryUtil.polygonArea(path)/polygonBounds.getArea());
    }

    public NestPath canCombineRegularity(NestPath nonConvex, NestPath convex) {
        double areaNonConvex = GeometryUtil.polygonArea(nonConvex);
        double areaConvex = GeometryUtil.polygonArea(convex);
        NestPath origin = null;
        try {
            origin = convex.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        int rotation = convex.getRotation();

            for(int i = 0; i < nonConvex.size(); i++){
                for(int j = 0; j < convex.size(); j++){
                    double shifx = nonConvex.get(i).x - convex.get(j).x;
                    double shify = nonConvex.get(i).y - convex.get(j).y;

                    NestPath shifPath = new NestPath();

                    for (int m = 0; m < convex.size(); m++) {
                        shifPath.add(new Segment(Math.rint(convex.get(m).x + shifx), Math.rint(convex.get(m).y + shify)));
                    }

                    if(GeometryUtil.interseccionPP(NestPathtoPiece(shifPath), NestPathtoPiece(nonConvex)) || GeometryUtil.insidePP(shifPath, nonConvex)  || GeometryUtil.insidePP2(shifPath, nonConvex) || GeometryUtil.insidePP2(nonConvex, shifPath) ){
                        continue;
                    }else{
                        Paths combinePath = unionPath(scaleUp2ClipperCoordinates(shifPath), scaleUp2ClipperCoordinates(nonConvex));
                        if(combinePath.size() > 1){
                            continue;
                        }
                        NestPath newConvex = toNestCoordinates(combinePath.get(0));

                        if((newConvex.getMaxX()-newConvex.getMinX()) > xObject || (newConvex.getMaxY()-newConvex.getMinY()) > yObject){
                            continue;
                        }
                        double fitness = calculateFitness(newConvex, nonConvex, convex, shifPath);
                        if(fitness >= threshold ){
                            long nonConvexId = nonConvex.getLongId();
                            long convexId = convex.getLongId();
                            String id1 = String.valueOf(nonConvexId);
                            String id2 = String.valueOf(convexId);
                            String newId = id1 + id2;
                            String smallStrId = convex.getStrId().compareTo(nonConvex.getStrId()) <= 0 ? convex.getStrId() : nonConvex.getStrId();
                            String bigStrId = convex.getStrId().compareTo(nonConvex.getStrId()) > 0 ? convex.getStrId() : nonConvex.getStrId();
                            String uionStr = smallStrId + "|" + bigStrId;
                            newConvex.setStrId(smallStrId + "&" + bigStrId);

                            Map<String, Double> temp = new HashMap<>();
                            temp.put(uionStr, fitness);
                            processUion.add(temp);

                            shifPath.setId(convex.getId());
                            shifPath.setArea(convex.getArea());
                            shifPath.setChild(convex.getChild());
                            shifPath.setStrId(convex.getStrId());
                            shifPath.leftChild = convex.leftChild;
                            shifPath.rightChild = convex.rightChild;
                            moveChildofPath(convex, shifx, shify);
                            newConvex.leftChild = convex;
                            newConvex.rightChild = nonConvex;
                            newConvex.child.add(convex);
                            newConvex.child.add(nonConvex);

                            return newConvex;
                        }

                    }
                }
            }

        return null;
    }

    public void moveChildofPath(NestPath path, double shifx, double shify){
        if(path.child.size() > 0){
            moveChildofPath(path.child.get(0), shifx, shify);
            moveChildofPath(path.child.get(1), shifx, shify);
        }
        for (int m = 0; m < path.size(); m++) {
            Segment segment = path.get(m);
            segment.x += shifx;
            segment.y += shify;
        }
    }

    public boolean whetherTwoPointsOverlap(NestPath shifPath, NestPath nonConvex) {
        int cnt = 0;
        for(int i = 0; i < shifPath.size(); i++){
            for(int j = 0; j < nonConvex.size(); j++){
                if(shifPath.get(i).x == nonConvex.get(j).x && shifPath.get(i).y == nonConvex.get(j).y){
                    cnt++;
                }
            }
        }
        if(cnt == 2){
            return true;
        }
        return false;
    }
}
