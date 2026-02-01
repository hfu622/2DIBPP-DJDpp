package com.example.demo.share;

import com.example.demo.utils.InputData;
import com.example.demo.utils.Points;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Instance {
    private List<Sheet> listaObjetos = new LinkedList<Sheet>();
    List<Piece> listapiezas = new LinkedList<Piece>();
    List<Piece> listapiezasFijas = new LinkedList<Piece>();
    private int xObjeto, yObjeto;
    private int numpiezas;
    private int noPzasAcomodar;
    private int Totalpiezas;
    public ResultVisual[] nuevo;
    private List<Vector<Sheet>> visualList = new ArrayList<>();


    public RePlacement rePlacement = new RePlacement();

    public Instance(int indi) {
        nuevo = new ResultVisual[indi];
    }

    public void obtainProblemInfo(InputData data) {
        Totalpiezas = 0;
        numpiezas = data.getNum();
        int width = data.getWidth();
        int height = data.getHeight();

        int scale = 10;
        if (width > 6000 && height > 6000) scale = 1;

        width *= scale;
        height *= scale;

        List<List<Points>> pieces = data.getPieces();
        for (List<Points> piece : pieces) {
            int[] vertices = new int[piece.size() * 2];
            for (int j = 0; j < piece.size(); j++) {
                vertices[j * 2] = piece.get(j).getX() * scale;
                vertices[j * 2 + 1] = piece.get(j).getY() * scale;
            }
            Piece piece1 = new Piece(vertices);
            Totalpiezas += piece1.getTotalSize();
            listapiezas.add(piece1);
            listapiezasFijas.add(piece1);
        }

        if (!listaObjetos.isEmpty())
            listaObjetos.clear();
        listaObjetos.add(new Sheet(width, height, 0));
        xObjeto = width;
        yObjeto = height;
        noPzasAcomodar = (int) (listapiezas.size());
        System.out.println("Pieces to place: " + noPzasAcomodar + " into objects of size " + xObjeto + " x " + yObjeto);
    }

    public void obtainProblem(File file) {
        RWfiles rw = new RWfiles();
        int[][] matriz = null;
        try {
            matriz = rw.obtainMatriz(file, 20);
        } catch (Exception e) {
            System.err.println("reading file error: " + file);
        }

        Totalpiezas = 0;
        numpiezas = matriz[0][0];

        for (int m = 0; m < numpiezas; m++) {
            int numLados = matriz[0][m + 2];
            int[] vertices = new int[numLados * 2];
            for (int i = 0; i < numLados * 2; i += 2) {
                vertices[i] = matriz[i + 1][m + 2];
                vertices[i + 1] = matriz[i + 2][m + 2];
            }
            Piece piece = new Piece(vertices);
            piece.setnumber(m);
            this.Totalpiezas += piece.getTotalSize();
            this.listapiezas.add(piece);
            this.listapiezasFijas.add(piece);
        }

        if (listaObjetos.size() > 0)
            listaObjetos.clear();
        listaObjetos.add(new Sheet(matriz[0][1], matriz[1][1], 0));
        xObjeto = matriz[0][1];
        yObjeto = matriz[1][1];
        noPzasAcomodar = (int) (listapiezas.size());
        System.out.println("Pieces to place: " + noPzasAcomodar + " into objects of size " + xObjeto + " x " + yObjeto);
    }

    public void obtainProblemExcel(File file) {
        try {
            Workbook workbook = Workbook.getWorkbook(file);
            jxl.Sheet sheet = workbook.getSheet(0);
            numpiezas = sheet.getRows() - 1;
            int k = 0;
            for (int i = 0; i < numpiezas; i = i + 2) {
                int len = sheet.getRow(i).length;

                List<Integer> list = new ArrayList<>();
                int numofVertices = 0;
                for (int j = 0; j < len; j++) {
                    if (sheet.getCell(j, i).getContents().isEmpty()) {
                        break;
                    }
                    numofVertices++;
                    int x = Integer.valueOf(sheet.getCell(j, i).getContents());
                    int y = Integer.valueOf(sheet.getCell(j, i + 1).getContents());
                    list.add(x);
                    list.add(y);
                }
                if (list.size() > 0) {
                    Integer[] vertices = list.toArray(new Integer[numofVertices * 2]);

                    Piece piece = new Piece(vertices);
                    piece.setnumber(k++);
                    this.Totalpiezas += piece.getTotalSize();
                    this.listapiezas.add(piece);
                }
            }
            xObjeto = Integer.valueOf(sheet.getCell(0, numpiezas).getContents());
            yObjeto = Integer.valueOf(sheet.getCell(1, numpiezas).getContents());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }

    public Results execute(int action, int indi, boolean graphVisual) {
        Results result = new Results();

        List<Piece> originPieceList = new ArrayList<>();
        for (Piece piece : listapiezas) {
            originPieceList.add(piece);
        }

        long startTime = System.currentTimeMillis();
        List<Double> resultList = new ArrayList<>();
        double aptitud = 0;
        rePlacement.threshold = 2;
        rePlacement.setOriginPieceList(originPieceList);

        rePlacement.processUion.clear();
        rePlacement.xObject = xObjeto;
        rePlacement.yObject = yObjeto;
        listapiezas = rePlacement.combineNonConvex();

        List<Map<String, Double>> processUnion = rePlacement.processUion;
        Collections.sort(processUnion, new Comparator<Map<String, Double>>() {
            @Override
            public int compare(Map<String, Double> o1, Map<String, Double> o2) {
                Double f1 = 0d;
                Double f2 = 0d;
                for (Map.Entry<String, Double> entry : o1.entrySet()) {
                    f1 = entry.getValue();
                }
                for (Map.Entry<String, Double> entry : o2.entrySet()) {
                    f2 = entry.getValue();
                }
                return f1.compareTo(f2);
            }
        });

        if (!listaObjetos.isEmpty())
            listaObjetos.clear();
        listaObjetos.add(new Sheet(xObjeto, yObjeto, 0));

        double currentResult;
        ControlHeuristics control = new ControlHeuristics();
        do {
            control.executeHeuristic(listapiezas, listaObjetos, 0);
        } while (!listapiezas.isEmpty());

        for (int i = 0; i < listaObjetos.size(); i++) {
            Sheet objk = (Sheet) listaObjetos.get(i);
            List<Piece> Lista2 = objk.getPzasInside();
            if (Lista2.size() == 0)
                listaObjetos.remove(i);
        }

        // plus: move pieces to left
        Sheet thisObjecto = listaObjetos.get(listaObjetos.size() - 1);
        List<Piece> tomovepieces = thisObjecto.getPzasInside();
        for (int step = 0; step < 5; step++) {
            Collections.sort(tomovepieces, new Comparator<Piece>() {
                @Override
                public int compare(Piece o1, Piece o2) {
                    return o1.getXmin() - o2.getXmin();
                }
            });
            for (Piece pc : tomovepieces) {
                int distHorizontal;
//            int xpos = pc.getXmin();
                int numgrande = 100000;
                do {
                    distHorizontal = HeuristicsPlacement.cercaniaHorOP(thisObjecto, pc);
                    if (distHorizontal > 0 && distHorizontal < numgrande) {
                        pc.moveDistance(distHorizontal, 3); // left
                    }

                } while ((distHorizontal > 0 && distHorizontal < numgrande));
            }
        }

        long endTime = System.currentTimeMillis();

        aptitud = control.calcularAptitud(listaObjetos);
//        int ax = (int) (aptitud * 1000.0);
//        aptitud = (double) ax / 1000.0;
        double utilization = control.calculateUtilization(listaObjetos);
        double Kvalue = control.calculateK(listaObjetos);
        int binnum = listaObjetos.size();
        System.out.println("Bin number:" + binnum);
        System.out.println("F value:" + aptitud);
        System.out.println("Utilization:" + utilization);
        System.out.println("K value:" + Kvalue);

        result.setUsedtime((int) (endTime - startTime));
        result.setListaObjetos(listaObjetos);
        result.setBinnum(binnum);
        result.setAptitude(aptitud);
        result.setUtilization(utilization);
        result.setKvalue(Kvalue);

        for (int i = 0; i < listaObjetos.size(); i++) {
            Sheet sheet = listaObjetos.get(i);
            List<Piece> pieceList = sheet.getPzasInside();

            boolean flag = true;
            while (flag) {
                boolean happen = false;
                for (int j = 0; j < pieceList.size(); j++) {
                    Piece piece = pieceList.get(j);
                    if (piece.child.size() > 0) {
                        double rotate = piece.getRotada();
                        piece.rotateCori(rotate);
                        int shifx = piece.coordX[0] - piece.getCoriX()[0];
                        int shify = piece.coordY[0] - piece.getCoriY()[0];
                        movereStore(piece, rotate, shifx, shify, pieceList);
                        pieceList.remove(piece);
                        happen = true;
                        break;
                    }
                }
                if (happen) {
                    flag = true;
                } else {
                    flag = false;
                }
            }
        }

        Vector<Sheet> listita = new Vector<Sheet>();
        for (int i = 0; i < listaObjetos.size(); i++) {
            listita.add((Sheet) (listaObjetos.get(i)));
        }
        if (graphVisual) {
            nuevo[indi] = new ResultVisual(listita);
            nuevo[indi].setSize(700, 650);
            nuevo[indi].setVisible(true);
        }
        visualList.add(listita);

        return result;
    }

    public double executeParallel(int action, int indi, boolean graphVisual) {
        int nThreads = 4;
        CountDownLatch countDownLatch = new CountDownLatch(nThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        Double[] results = new Double[nThreads];
        List<List<Piece>> parallelPieceList = new ArrayList<>();
        for (int j = 0; j < nThreads; j++) {
            List<Piece> pieceList = new LinkedList<>();
            for (Piece piece : listapiezas) {
                try {
                    pieceList.add((Piece) piece.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
            parallelPieceList.add(pieceList);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < nThreads; i++) {
            int id = i;
            executorService.submit(() -> {
                System.out.println("current Thread:" + Thread.currentThread().getName() + " begin");
                results[id] = singleThreadTask(indi, graphVisual, parallelPieceList.get(id));
                countDownLatch.countDown();
                System.out.println("current Thread:" + Thread.currentThread().getName() + " countdown");
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        return Arrays.stream(results).max(Double::compareTo).get();
    }

    private double singleThreadTask(int indi, boolean graphVisual, List<Piece> originPieceList) {
        Queue<String> tabuQueue = new LinkedList<>();
        double initThreshold = 2;
        int tabuSize = 5;

        RePlacement rePlacement = new RePlacement();
        rePlacement.findConvexAndNonconvex(originPieceList, rePlacement.convexList, rePlacement.nonConvexList);

        List<Double> resultList = new ArrayList<>();
        double bestResult = 0;
        rePlacement.threshold = 2;
        rePlacement.smallThreshold = 0.4;
        rePlacement.tabuQueue = tabuQueue;
        int iteration = 1;
        bestResult = tabuSearch(iteration, indi, graphVisual, tabuQueue, initThreshold, tabuSize, originPieceList, resultList, bestResult, rePlacement);

        System.out.println("current Thread: " + Thread.currentThread().getName() + ".  process:" + resultList + ".  best result:" + bestResult);

        return bestResult;
    }

    private double tabuSearch(int iteration, int indi, boolean graphVisual, Queue<String> tabuQueue, double initThreshold, int tabuSize, List<Piece> originPieceList, List<Double> resultList, double bestResult, RePlacement rePlacement) {
        List<Piece> pieceListParallel = new LinkedList<>();
        for (int it = 0; it < iteration; it++) {
            rePlacement.setOriginPieceList(originPieceList);
            rePlacement.threshold -= it * 0.1;
            if (rePlacement.threshold < rePlacement.smallThreshold) {
                rePlacement.threshold = initThreshold;
            }
            rePlacement.processUion.clear();
            rePlacement.xObject = xObjeto;
            rePlacement.yObject = yObjeto;
            pieceListParallel = rePlacement.combineNonConvex();

            List<Map<String, Double>> processUnion = rePlacement.processUion;
            Collections.sort(processUnion, new Comparator<Map<String, Double>>() {
                @Override
                public int compare(Map<String, Double> o1, Map<String, Double> o2) {
                    Double f1 = 0d;
                    Double f2 = 0d;
                    for (Map.Entry<String, Double> entry : o1.entrySet()) {
                        f1 = entry.getValue();
                    }
                    for (Map.Entry<String, Double> entry : o2.entrySet()) {
                        f2 = entry.getValue();
                    }
                    if (f1 - f2 < 0) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            if (it % tabuSize == 0) {
                tabuQueue.poll();
            }
            if (!processUnion.isEmpty()) {
                String fit = (String) processUnion.get(0).keySet().stream().toArray()[0];
                if (queueContains(tabuQueue, fit)) {
                    int size = processUnion.size();
                    int randomNum = (int) (Math.random() * size);
                    fit = (String) processUnion.get(randomNum).keySet().stream().toArray()[0];
                    while (tabuQueue.contains(fit)) {
                        randomNum = (int) (Math.random() * size);
                        fit = (String) processUnion.get(randomNum).keySet().stream().toArray()[0];
                    }
                    tabuQueue.offer(fit);
                } else {
                    tabuQueue.offer(fit);
                }
            }

            List<Sheet> listaObjetos = new LinkedList<>();
            if (listaObjetos.size() > 0)
                listaObjetos.clear();
            listaObjetos.add(new Sheet(xObjeto, yObjeto, 0));

            double currentResult;
            ControlHeuristics control = new ControlHeuristics();
            do {
                control.executeHeuristic(pieceListParallel, listaObjetos, 0);
            } while (pieceListParallel.size() > 0);

            for (int i = 0; i < listaObjetos.size(); i++) {
                Sheet objk = (Sheet) listaObjetos.get(i);
                List<Piece> Lista2 = objk.getPzasInside();
                if (Lista2.size() == 0)
                    listaObjetos.remove(i);
            }
            currentResult = control.calcularAptitud(listaObjetos);
            int ax = (int) (currentResult * 1000.0);
            currentResult = (double) ax / 1000.0;
            resultList.add(currentResult);
            if (currentResult > bestResult) {
                bestResult = currentResult;
            }

            for (int i = 0; i < listaObjetos.size(); i++) {
                Sheet sheet = listaObjetos.get(i);
                List<Piece> pieceList = sheet.getPzasInside();

                boolean flag = true;
                while (flag) {
                    boolean happen = false;
                    for (int j = 0; j < pieceList.size(); j++) {
                        Piece piece = pieceList.get(j);
                        if (piece.child.size() > 0) {
                            double rotate = piece.getRotada();
                            piece.rotateCori(rotate);
                            int shifx = piece.coordX[0] - piece.getCoriX()[0];
                            int shify = piece.coordY[0] - piece.getCoriY()[0];
                            movereStore(piece, rotate, shifx, shify, pieceList);
                            pieceList.remove(piece);
                            happen = true;
                            break;
                        }
                    }
                    if (happen) {
                        flag = true;
                    } else {
                        flag = false;
                    }
                }
            }

            ResultVisual[] nuevo = new ResultVisual[4];
            if (graphVisual) {
                Vector<Sheet> listita = new Vector<Sheet>();
                for (int i = 0; i < listaObjetos.size(); i++) {
                    listita.add((Sheet) (listaObjetos.get(i)));
                }
                nuevo[indi] = new ResultVisual(listita);
                nuevo[indi].setSize(700, 650);
                nuevo[indi].setVisible(true);
            }

        }
        return bestResult;
    }

    private double getBestResult(int indi, boolean graphVisual, List<Piece> originPieceList, List<Double> resultList, double bestResult) {
        if (listaObjetos.size() > 0)
            listaObjetos.clear();
        listaObjetos.add(new Sheet(xObjeto, yObjeto, 0));
        for (Piece piece : originPieceList) {
            listapiezas.add(piece);
        }

        double aptitud;
        ControlHeuristics control = new ControlHeuristics();
        do {
            control.executeHeuristic(listapiezas, listaObjetos, 0);
        } while (listapiezas.size() > 0);

        for (int i = 0; i < listaObjetos.size(); i++) {
            Sheet objk = (Sheet) listaObjetos.get(i);
            List<Piece> Lista2 = objk.getPzasInside();
            if (Lista2.size() == 0)
                listaObjetos.remove(i);
        }

        aptitud = control.calcularAptitud(listaObjetos);
        int ax = (int) (aptitud * 1000.0);
        aptitud = (double) ax / 1000.0;
        resultList.add(aptitud);
        if (aptitud > bestResult) {
            bestResult = aptitud;
        }

        return bestResult;
    }

    private void movereStore(Piece piece, double rotate, int shifx, int shify, List<Piece> pieceList) {
        if (piece.child.size() > 0) {
            movereStore(piece.child.get(0), rotate, shifx, shify, pieceList);
            movereStore(piece.child.get(1), rotate, shifx, shify, pieceList);
        }

        if (piece.child.size() == 0) {
            piece.rotate(rotate);
            for (int i = 0; i < piece.coordX.length; i++) {
                piece.coordX[i] += shifx;
                piece.coordY[i] += shify;
            }
            pieceList.add(piece);
        }

    }

    private void reExecute(List<Piece> listapiezas, List<Sheet> listaObjetos, int action) {
        listapiezas.clear();
        List<Sheet> fullObjectList = new ArrayList<>();
        for (int i = 0; i < listaObjetos.size(); i++) {
            if (i == listaObjetos.size() - 1) {
                fullObjectList.add(listaObjetos.get(i));
                continue;
            }
            if (listaObjetos.get(i).getFreeArea() == 0) {
                fullObjectList.add(listaObjetos.get(i));
                continue;
            }
            List<Piece> pzasInside = listaObjetos.get(i).getPzasInside();
            for (int j = 0; j < pzasInside.size(); j++) {
                listapiezas.add(pzasInside.get(j));
            }
        }
        listaObjetos.clear();
        listaObjetos.add(new Sheet(1000, 1000, 0));
        ControlHeuristics control = new ControlHeuristics();
        do {
            control.executeHeuristic(listapiezas, listaObjetos, action);
        } while (listapiezas.size() > 0);

        for (int i = 0; i < fullObjectList.size(); i++) {
            listaObjetos.add(fullObjectList.get(i));
        }
    }

    public int numeroObjetos() {
        return listaObjetos.size();
    }

    public void moveAllPieceToBottomLeft(List<Piece> listapiezas) {
        for (int i = 0; i < listapiezas.size(); i++) {
            Piece piece = listapiezas.get(i);
            piece.moveToXY(0, 0, 2);
            if (piece.child.size() != 0) {
                for (int j = 0; j < piece.child.size(); j++) {
                    piece.child.get(j).moveToXY(0, 0, 2);
                }
            }
        }
    }

    public boolean queueContains(Queue<String> queue, String str) {
        for (String s : queue) {
            if (s.equals(str)) {
                return true;
            }
        }
        return false;
    }

    public Results ejecutaAccion(int action, int indi, boolean graficar) {
        long startTime = System.currentTimeMillis();
        Results result = new Results();

        if (!listaObjetos.isEmpty())
            listaObjetos.clear();
        listaObjetos.add(new Sheet(xObjeto, yObjeto, 0));
        double aptitud;
        ControlHeuristics control = new ControlHeuristics();
        do {
            control.executeHeuristic(listapiezas, listaObjetos, action);
        } while (!listapiezas.isEmpty());

        long endTime = System.currentTimeMillis();
        System.out.println("执行时间：" + (endTime - startTime) + "ms");

        for (int i = 0; i < listaObjetos.size(); i++) {
            Sheet objk = (Sheet) listaObjetos.get(i);
            List<Piece> Lista2 = objk.getPzasInside();
            if (Lista2.size() == 0)
                listaObjetos.remove(i);
        }

        aptitud = control.calcularAptitud(listaObjetos);
        double utilization = control.calculateUtilization(listaObjetos);
        double Kvalue = control.calculateK(listaObjetos);
        int binnum = listaObjetos.size();
        System.out.println("Bin number:" + binnum);
        System.out.println("F value:" + aptitud);
        System.out.println("Utilization:" + utilization);
        System.out.println("K value:" + Kvalue);

        result.setUsedtime((int) (endTime - startTime));
        result.setListaObjetos(listaObjetos);
        result.setBinnum(binnum);
        result.setAptitude(aptitud);
        result.setUtilization(utilization);
        result.setKvalue(Kvalue);

        return result;
    }
}

class Results {
    private List<Sheet> listaObjetos = new LinkedList<Sheet>();
    private int usedtime;
    private double aptitude;
    private double utilization;
    private double Kvalue;
    private int binnum;

    public Results(List<Sheet> listaObjetos, int usedtime, double aptitude, double utilization, double Kvalue, int binnum) {
        this.listaObjetos = listaObjetos;
        this.usedtime = usedtime;
        this.aptitude = aptitude;
        this.utilization = utilization;
        this.Kvalue = Kvalue;
        this.binnum = binnum;
    }

    public Results() {
    }

    public void setListaObjetos(List<Sheet> listaObjetos) {
        this.listaObjetos = listaObjetos;
    }

    public List<Sheet> getListaObjetos() {
        return listaObjetos;
    }

    public void setUsedtime(int usedtime) {
        this.usedtime = usedtime;
    }

    public int getUsedtime() {
        return usedtime;
    }

    public void setAptitude(double aptitude) {
        this.aptitude = aptitude;
    }

    public double getAptitude() {
        return aptitude;
    }

    public void setUtilization(double utilization) {
        this.utilization = utilization;
    }

    public double getUtilization() {
        return utilization;
    }

    public void setKvalue(double Kvalue) {
        this.Kvalue = Kvalue;
    }

    public double getKvalue() {
        return Kvalue;
    }

    public void setBinnum(int binnum) {
        this.binnum = binnum;
    }

    public int getBinnum() {
        return binnum;
    }

}