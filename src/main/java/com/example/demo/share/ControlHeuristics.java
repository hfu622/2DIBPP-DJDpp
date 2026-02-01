package com.example.demo.share;

import java.util.List;


public class ControlHeuristics {

    Heuristics h = new Heuristics();

    public ControlHeuristics() {
    }

    public void executeHeuristic(List<Piece> listapiezas, List<Sheet> listaObjetos, int heuristica) {

        Sheet sheet = (Sheet) listaObjetos.get(0);
        int xObject = sheet.getXmax();
        int yObject = sheet.getYmax();


        switch (heuristica) {
            case 1:
                h.Djang_and_Finch(listapiezas, listaObjetos, xObject, yObject, "MALBR", 0.333, 0);
                break;
            case 0:
                h.Djang_and_Finch(listapiezas, listaObjetos, xObject, yObject, "MALBR", 0.333, 1);
                break;
            case 3:
                h.First_Fit_Decreasing(listapiezas, listaObjetos, xObject, yObject, "BL");
                break;
            case 6:
                h.Filler(listapiezas, listaObjetos, xObject, yObject, "EC");
                break;
            case 2:
                h.Best_Fit_Decreasing(listapiezas, listaObjetos, xObject, yObject, "EC2");
                break;
            case 4:
                h.Djang_and_Finch(listapiezas, listaObjetos, xObject, yObject, "BL", 0.3333, 0);
                break;
            case 5:
                h.Djang_and_Finch(listapiezas, listaObjetos, xObject, yObject, "EC2", 0.5, 0);
                break;
            default:
                h.Djang_and_Finch(listapiezas, listaObjetos, xObject, yObject, "BL", 0.3333, 0);
                break;
        }
    }


    public void Imprime_Resultado(List<Sheet> listaObjetos) {
        Sheet objtemp;
        List<Piece> ListaPiezaInside;
        System.out.println(listaObjetos.size());
        for (int i = 0; i < listaObjetos.size(); i++) {
            objtemp = (Sheet) listaObjetos.get(i);
            ListaPiezaInside = objtemp.getPzasInside();
            for (int j = 0; j < ListaPiezaInside.size(); j++) {
                Piece piezatemp = (Piece) ListaPiezaInside.get(j);
                System.out.println("Sheet " + i + "= " + piezatemp.getnumber());
            }
        }
        for (int i = 0; i < listaObjetos.size(); i++) {
            objtemp = (Sheet) listaObjetos.get(i);
            ListaPiezaInside = objtemp.getPzasInside();
            for (int j = 0; j < ListaPiezaInside.size(); j++) {
                Piece piezatemp = (Piece) ListaPiezaInside.get(j);
                System.out.println("Coordenada (xmax, ymax)" + piezatemp.getnumber() + " = " + piezatemp.getXmax() + " , " + piezatemp.getYmax());
                System.out.println("Coordenada (xmin, ymin)" + piezatemp.getnumber() + " = " + piezatemp.getXmin() + " , " + piezatemp.getYmin());
                int resta1 = piezatemp.getXmax() - piezatemp.getXmin();
                int resta2 = piezatemp.getYmax() - piezatemp.getYmin();
                System.out.println("RESTA " + piezatemp.getnumber() + "= " + resta1 + " " + resta2);
            }
        }
        for (int i = 0; i < listaObjetos.size(); i++) {
            objtemp = (Sheet) listaObjetos.get(i);
            ListaPiezaInside = objtemp.getPzasInside();
            for (int j = 0; j < ListaPiezaInside.size(); j++) {
                Piece piezatemp = (Piece) ListaPiezaInside.get(j);
                if (piezatemp.getnumber() == 66) {

                    System.out.println("Coordenada (xmax, ymax)" + piezatemp.getnumber() + " = " + piezatemp.getXmax() + " , " + piezatemp.getYmax());
                    System.out.println("Coordenada (xmin, ymin)" + piezatemp.getnumber() + " = " + piezatemp.getXmin() + " , " + piezatemp.getYmin());
                    int resta1 = piezatemp.getXmax() - piezatemp.getXmin();
                    int resta2 = piezatemp.getYmax() - piezatemp.getYmin();
                    System.out.println("RESTA " + piezatemp.getnumber() + "= " + resta1 + " " + resta2);
                }
            }
        }
    }

    public double calcularAptitud(List<Sheet> listaObjetos) {
        Sheet objtemp;
        List<Piece> ListaPiezasInside;
        Piece piezatemp;
        double[] Pu;
        double aux, aux2;
        double aptitud = 0;
        int api, ao;

        Pu = new double[listaObjetos.size()];
        for (int i = 0; i < listaObjetos.size(); i++) {
            objtemp = (Sheet) listaObjetos.get(i);
            ListaPiezasInside = objtemp.getPzasInside();
            Pu[i] = 0;
            ao = objtemp.gettotalsize();
            for (int j = 0; j < ListaPiezasInside.size(); j++) {
                piezatemp = (Piece) ListaPiezasInside.get(j);
                api = piezatemp.getTotalSize();
                aux = (double) api / ao;
                Pu[i] = Pu[i] + aux;
            }
            aux2 = Pu[i];
            Pu[i] = Math.pow(aux2, 2);
            aptitud = aptitud + Pu[i];
        }
        aptitud = (double) aptitud / listaObjetos.size();
        return aptitud;
    }

    public double calculateUtilization(List<Sheet> listaObjetos) {
        Sheet objtemp;
        List<Piece> ListaPiezasInside;
        Piece piezatemp;
        double totalsize = 0;
        for (int i = 0; i < listaObjetos.size(); i++) {
            objtemp = listaObjetos.get(i);
            ListaPiezasInside = objtemp.getPzasInside();
            for (int j = 0; j < ListaPiezasInside.size(); j++) {
                piezatemp = ListaPiezasInside.get(j);
                totalsize += piezatemp.getTotalSize();

            }
        }
        double binArea = listaObjetos.get(0).getXmax() * listaObjetos.get(0).getYmax();
        return totalsize / binArea / listaObjetos.size();
    }

    public double calculateK(List<Sheet> listaObjetos) {
        double minR = 1e5;
        if (listaObjetos.size() > 1) {
            int objnum = listaObjetos.size();
            Sheet objtemp = listaObjetos.get(objnum - 1);
            double maxx = -1e5, maxy = -1e5;
            for (int i = 0; i < objtemp.getPzasInside().size(); i++) {
                Piece piezatemp = objtemp.getPzasInside().get(i);
                for (int j = 0; j < piezatemp.getvertices(); j++) {
                    int px = piezatemp.coordX[j];
                    int py = piezatemp.coordY[j];
                    maxx = Math.max(maxx, px);
                    maxy = Math.max(maxy, py);
                }
            }
            double R = Math.min(maxx / objtemp.getXmax(), maxy / objtemp.getYmax());
            return objnum - 1 + R;
        } else {
            Sheet objtemp = listaObjetos.get(0);
            double maxx = -1e5, maxy = -1e5;
            for (int i = 0; i < objtemp.getPzasInside().size(); i++) {
                Piece piezatemp = objtemp.getPzasInside().get(i);
                for (int j = 0; j < piezatemp.getvertices(); j++) {
                    int px = piezatemp.coordX[j];
                    int py = piezatemp.coordY[j];
                    maxx = Math.max(maxx, px);
                    maxy = Math.max(maxy, py);
                }
            }
            double R = Math.min(maxx / objtemp.getXmax(), maxy / objtemp.getYmax());
            return R;
        }
    }

}